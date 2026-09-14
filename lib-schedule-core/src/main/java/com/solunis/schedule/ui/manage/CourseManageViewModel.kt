package com.solunis.schedule.ui.manage

import android.app.Application
import androidx.lifecycle.*
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.database.entity.CourseBaseBean
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.CourseDetailBean
import com.solunis.schedule.data.repository.CourseRepository
import kotlinx.coroutines.launch

class CourseManageViewModel(
    application: Application,
    private val courseRepository: CourseRepository,
    private val database: AppDatabase
) : AndroidViewModel(application) {

    private val tableId: Int = 1

    val courses: LiveData<List<CourseBean>> = courseRepository.getCoursesByTableId(tableId)

    fun addCourse(name: String, room: String, day: Int, startNode: Int, step: Int) {
        viewModelScope.launch {
            val existing = courseRepository.getCoursesByTableIdSync(tableId)
            val newId = (existing.maxOfOrNull { it.id } ?: 0) + 1
            val colorIdx = newId % 7
            courseRepository.insertCourse(
                CourseBaseBean(id = newId, courseName = name, color = "$colorIdx", tableId = tableId),
                CourseDetailBean(id = newId, day = day, room = room, teacher = "", startNode = startNode, step = step, startWeek = 1, endWeek = 20, type = 0, tableId = tableId)
            )
        }
    }

    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            courseRepository.deleteCourse(courseId, tableId)
        }
    }
}

class CourseManageViewModelFactory(
    private val application: Application,
    private val courseRepository: CourseRepository,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CourseManageViewModel(application, courseRepository, database) as T
    }
}
