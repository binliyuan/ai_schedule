package com.solunis.schedule.ui.schedule

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.database.entity.*
import com.solunis.schedule.data.local.TokenManager
import com.solunis.schedule.data.network.RetrofitClient
import com.solunis.schedule.data.repository.CourseRepository
import com.solunis.schedule.data.repository.HomeworkRepository
import com.solunis.schedule.data.repository.TableRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class SyncState { IDLE, LOADING, SUCCESS, ERROR }

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val courseRepository = CourseRepository(database.courseDao(), RetrofitClient.apiService)
    private val tableRepository = TableRepository(database.tableDao())
    private val homeworkRepository = HomeworkRepository(database.homeworkDao())

    val currentTable: LiveData<TableBean?> = tableRepository.getDefaultTable()

    private val _currentWeek = MutableLiveData(1)
    val currentWeek: LiveData<Int> = _currentWeek

    private val _todayDayOfWeek = MutableLiveData(LocalDate.now().dayOfWeek.value)
    val todayDayOfWeek: LiveData<Int> = _todayDayOfWeek

    private val _selectedCourse = MutableLiveData<CourseBean?>(null)
    val selectedCourse: LiveData<CourseBean?> = _selectedCourse

    private val _showDetailOverlay = MutableLiveData(false)
    val showDetailOverlay: LiveData<Boolean> = _showDetailOverlay

    private val _showHomeworkPopup = MutableLiveData(false)
    val showHomeworkPopup: LiveData<Boolean> = _showHomeworkPopup

    private val _syncState = MutableLiveData(SyncState.IDLE)
    val syncState: LiveData<SyncState> = _syncState

    val allCourses: LiveData<List<CourseBean>> = currentTable.switchMap { table ->
        table?.let { courseRepository.getCoursesByTableId(it.id) }
            ?: MutableLiveData(emptyList())
    }

    val allHomework: LiveData<List<HomeworkBean>> = currentTable.switchMap { table ->
        table?.let { homeworkRepository.getAllHomework(it.id) }
            ?: MutableLiveData(emptyList())
    }

    val timeDetails: LiveData<List<TimeDetailBean>> = currentTable.switchMap { table ->
        table?.let { database.timeDao().getTimeDetails(it.timeTable) }
            ?: MutableLiveData(emptyList())
    }

    init {
        calculateCurrentWeek()
    }

    private fun calculateCurrentWeek() {
        viewModelScope.launch {
            val table = tableRepository.getDefaultTableSync()
            if (table != null && table.startDate.isNotEmpty()) {
                val startDate = LocalDate.parse(table.startDate, DateTimeFormatter.ISO_LOCAL_DATE)
                val today = LocalDate.now()
                val daysBetween = ChronoUnit.DAYS.between(startDate, today)
                val week = (daysBetween / 7 + 1).toInt().coerceIn(1, table.maxWeek)
                _currentWeek.postValue(week)
            }
        }
    }

    fun syncScheduleData() {
        viewModelScope.launch {
            val table = tableRepository.getDefaultTableSync() ?: return@launch
            val tableId = table.id

            if (!TokenManager.isLoggedIn()) {
                Log.d("SyncSchedule", "Not logged in, using local data")
                val localCourses = courseRepository.getCoursesByTableIdSync(tableId)
                if (localCourses.isEmpty()) {
                    insertSampleData(tableId)
                }
                _syncState.postValue(SyncState.IDLE)
                return@launch
            }

            _syncState.postValue(SyncState.LOADING)
            Log.d("SyncSchedule", "Starting sync for table $tableId")

            val networkResult = courseRepository.syncFromNetwork(tableId)

            if (networkResult.isSuccess) {
                _syncState.postValue(SyncState.SUCCESS)
                Log.d("SyncSchedule", "Network sync success: ${networkResult.getOrNull()} courses")
            } else {
                Log.w("SyncSchedule", "Network failed, checking local data")
                val localCourses = courseRepository.getCoursesByTableIdSync(tableId)
                if (localCourses.isEmpty()) {
                    insertSampleData(tableId)
                }
                _syncState.postValue(SyncState.ERROR)
            }
        }
    }

    private suspend fun insertSampleData(tableId: Int) {
        data class SampleCourse(val id: Int, val name: String, val color: String, val details: List<Triple<Int, Int, String>>)

        val samples = listOf(
            SampleCourse(1, "高等数学", "0", listOf(Triple(1, 1, "A301"), Triple(3, 1, "A301"))),
            SampleCourse(2, "大学物理", "1", listOf(Triple(1, 5, "B202"), Triple(4, 1, "B202"))),
            SampleCourse(3, "英语听力", "4", listOf(Triple(1, 9, "C105"))),
            SampleCourse(4, "线性代数", "3", listOf(Triple(2, 3, "A205"), Triple(5, 1, "A205"))),
            SampleCourse(5, "计算机", "4", listOf(Triple(2, 7, "D401"))),
            SampleCourse(6, "程序设计", "2", listOf(Triple(3, 5, "E302"), Triple(5, 3, "E302"))),
            SampleCourse(7, "体育", "5", listOf(Triple(3, 9, "操场"))),
            SampleCourse(8, "思想政治", "6", listOf(Triple(4, 3, "F101"))),
            SampleCourse(9, "英语写作", "4", listOf(Triple(4, 7, "C203"))),
            SampleCourse(10, "实验物理", "1", listOf(Triple(5, 7, "G201"))),
            SampleCourse(11, "摄影", "5", listOf(Triple(6, 3, "H102"))),
            SampleCourse(12, "自习", "3", listOf(Triple(7, 5, "图书馆")))
        )

        samples.forEach { sample ->
            database.courseDao().insertCourseBase(
                CourseBaseBean(id = sample.id, courseName = sample.name, color = sample.color, tableId = tableId)
            )
            sample.details.forEach { (day, startNode, room) ->
                val step = when {
                    sample.name == "程序设计" && day == 3 -> 3
                    sample.name == "实验物理" -> 3
                    else -> 2
                }
                database.courseDao().insertCourseDetail(
                    CourseDetailBean(
                        id = sample.id, day = day, room = room,
                        teacher = "", startNode = startNode, step = step,
                        startWeek = 1, endWeek = 20, type = 0, tableId = tableId
                    )
                )
            }
        }
    }

    fun getFormattedDate(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    fun getDayOfWeekName(): String {
        val names = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        return names[LocalDate.now().dayOfWeek.value - 1]
    }

    fun getTodayCoursesCount(courses: List<CourseBean>): Int {
        val dow = LocalDate.now().dayOfWeek.value
        val week = _currentWeek.value ?: 1
        return courses.count { it.day == dow && it.startWeek <= week && it.endWeek >= week }
    }

    fun getCurrentCourse(courses: List<CourseBean>, timeDetails: List<TimeDetailBean>): CourseBean? {
        val dow = LocalDate.now().dayOfWeek.value
        val week = _currentWeek.value ?: 1
        val now = java.time.LocalTime.now()
        return courses.filter {
            it.day == dow && it.startWeek <= week && it.endWeek >= week
        }.firstOrNull { course ->
            val startTime = timeDetails.find { it.node == course.startNode }
            val endNode = course.startNode + course.step - 1
            val endTime = timeDetails.find { it.node == endNode }
            if (startTime != null && endTime != null) {
                val start = java.time.LocalTime.parse(startTime.startTime)
                val end = java.time.LocalTime.parse(endTime.endTime)
                now in start..end
            } else false
        }
    }

    fun selectCourse(course: CourseBean?) { _selectedCourse.value = course }
    fun showDetailOverlay(show: Boolean) { _showDetailOverlay.value = show }
    fun showHomeworkPopup(show: Boolean) { _showHomeworkPopup.value = show }

    fun addHomework(courseId: Int, tableId: Int, text: String) {
        viewModelScope.launch {
            homeworkRepository.insertHomework(
                HomeworkBean(courseId = courseId, tableId = tableId, text = text)
            )
        }
    }

    fun toggleHomework(homework: HomeworkBean) {
        viewModelScope.launch {
            homeworkRepository.updateHomework(homework.copy(done = !homework.done))
        }
    }

    fun updateCourseName(courseId: Int, tableId: Int, newName: String) {
        viewModelScope.launch {
            val courses = courseRepository.getCoursesByTableIdSync(tableId)
            val base = courses.firstOrNull { it.id == courseId }
            if (base != null) {
                database.courseDao().updateCourseBase(
                    CourseBaseBean(id = courseId, courseName = newName, color = base.color, tableId = tableId)
                )
            }
        }
    }

    fun updateCourseRoom(courseId: Int, tableId: Int, day: Int, startNode: Int, newRoom: String) {
        viewModelScope.launch {
            val courses = courseRepository.getCoursesByTableIdSync(tableId)
            val detail = courses.firstOrNull {
                it.id == courseId && it.day == day && it.startNode == startNode
            }
            if (detail != null) {
                database.courseDao().updateCourseDetail(
                    CourseDetailBean(
                        id = courseId, day = day, room = newRoom,
                        teacher = detail.teacher, startNode = startNode,
                        step = detail.step, startWeek = detail.startWeek,
                        endWeek = detail.endWeek, type = detail.type, tableId = tableId
                    )
                )
            }
        }
    }
}
