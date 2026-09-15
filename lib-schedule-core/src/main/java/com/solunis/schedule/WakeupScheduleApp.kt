package com.solunis.schedule

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.solunis.schedule.data.ai.AiConfig
import com.solunis.schedule.data.ai.SkillLoader
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.local.TokenManager
import com.solunis.schedule.service.ScheduleNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WakeupScheduleApp : Application() {

    lateinit var database: AppDatabase
        private set

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        TokenManager.init(this)
        AiConfig.initDefaults(this)
        createNotificationChannels()
        appScope.launch { SkillLoader.syncFromServer(this@WakeupScheduleApp) }
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            ScheduleNotificationService.CHANNEL_ID,
            "课程提醒",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "显示当前正在进行的课程信息"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
