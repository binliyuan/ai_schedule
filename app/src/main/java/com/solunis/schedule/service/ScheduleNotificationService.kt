package com.solunis.schedule.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.solunis.schedule.HomeActivity
import com.solunis.schedule.R
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.TimeDetailBean
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ScheduleNotificationService : Service() {

    companion object {
        private const val TAG = "ScheduleNotifService"
        const val CHANNEL_ID = "schedule_service"
        const val NOTIFICATION_ID = 1002

        var isRunning = false
            private set
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startForeground(NOTIFICATION_ID, buildNotification("课程表", "正在获取课程信息..."))
            isRunning = true

            if (tickerJob == null || tickerJob?.isActive != true) {
                tickerJob = serviceScope.launch {
                    refreshNotification()
                    while (isActive) {
                        delay(60_000)
                        refreshNotification()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        serviceScope.cancel()
        isRunning = false
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }

    private suspend fun refreshNotification() {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val table = db.tableDao().getDefaultTableSync()
                if (table == null || table.startDate.isEmpty()) {
                    postNotification("课程表", "暂无课表数据")
                    return@withContext
                }

                val startDate = LocalDate.parse(table.startDate, DateTimeFormatter.ISO_LOCAL_DATE)
                val daysBetween = ChronoUnit.DAYS.between(startDate, LocalDate.now())
                val currentWeek = (daysBetween / 7 + 1).toInt().coerceIn(1, table.maxWeek)

                val courses = db.courseDao().getCoursesByTableIdSync(table.id)
                val timeDetails = db.timeDao().getTimeDetailsSync(table.timeTable)

                val dow = LocalDate.now().dayOfWeek.value
                val now = LocalTime.now()

                val currentCourse = getCurrentCourse(courses, timeDetails, currentWeek, dow, now)

                if (currentCourse != null) {
                    val endNode = currentCourse.startNode + currentCourse.step - 1
                    val startTime = timeDetails.find { it.node == currentCourse.startNode }
                    val endTime = timeDetails.find { it.node == endNode }

                    val timeText = if (startTime != null && endTime != null) {
                        "${startTime.startTime} - ${endTime.endTime}"
                    } else ""

                    val remainingMin = if (endTime != null) {
                        Duration.between(now, LocalTime.parse(endTime.endTime)).toMinutes().coerceAtLeast(0)
                    } else 0L

                    postNotification(
                        "${currentCourse.courseName} - ${currentCourse.room}",
                        "$timeText | 剩余 ${remainingMin} 分钟"
                    )
                } else {
                    val todayCourses = courses.filter {
                        it.day == dow && it.startWeek <= currentWeek && it.endWeek >= currentWeek
                    }
                    val nextCourse = todayCourses.firstOrNull { course ->
                        val st = timeDetails.find { it.node == course.startNode }
                        st != null && LocalTime.parse(st.startTime).isAfter(now)
                    }

                    if (nextCourse != null) {
                        val nextStart = timeDetails.find { it.node == nextCourse.startNode }
                        postNotification(
                            "当前无课程",
                            "下节课: ${nextCourse.courseName} ${nextCourse.room} ${nextStart?.startTime ?: ""}"
                        )
                    } else {
                        postNotification("当前无课程", "今天没有更多课程了")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh notification", e)
                postNotification("课程表", "课程信息加载失败")
            }
        }
    }

    private fun getCurrentCourse(
        courses: List<CourseBean>,
        timeDetails: List<TimeDetailBean>,
        week: Int,
        dow: Int,
        now: LocalTime
    ): CourseBean? {
        return courses.filter {
            it.day == dow && it.startWeek <= week && it.endWeek >= week
        }.firstOrNull { course ->
            val startTime = timeDetails.find { it.node == course.startNode }
            val endNode = course.startNode + course.step - 1
            val endTime = timeDetails.find { it.node == endNode }
            if (startTime != null && endTime != null) {
                val start = LocalTime.parse(startTime.startTime)
                val end = LocalTime.parse(endTime.endTime)
                now in start..end
            } else false
        }
    }

    private fun postNotification(title: String, text: String) {
        try {
            val notification = buildNotification(title, text)
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted", e)
        }
    }

    private fun buildNotification(title: String, text: String): Notification {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }
}
