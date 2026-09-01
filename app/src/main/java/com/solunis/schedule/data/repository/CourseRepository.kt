package com.solunis.schedule.data.repository

import androidx.lifecycle.LiveData
import com.solunis.schedule.data.database.dao.CourseDao
import com.solunis.schedule.data.database.entity.CourseBaseBean
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.CourseDetailBean

class CourseRepository(private val courseDao: CourseDao) {

    fun getCoursesByTableId(tableId: Int): LiveData<List<CourseBean>> =
        courseDao.getCoursesByTableId(tableId)

    suspend fun getCoursesByTableIdSync(tableId: Int): List<CourseBean> =
        courseDao.getCoursesByTableIdSync(tableId)

    fun getCoursesByDay(tableId: Int, day: Int): LiveData<List<CourseBean>> =
        courseDao.getCoursesByDay(tableId, day)

    suspend fun insertCourse(base: CourseBaseBean, detail: CourseDetailBean) {
        courseDao.insertCourseBase(base)
        courseDao.insertCourseDetail(detail)
    }

    suspend fun updateCourse(base: CourseBaseBean, detail: CourseDetailBean) {
        courseDao.updateCourseBase(base)
        courseDao.updateCourseDetail(detail)
    }

    suspend fun deleteCourse(id: Int, tableId: Int) {
        courseDao.deleteCourseBase(id, tableId)
    }

    suspend fun deleteAllByTable(tableId: Int) {
        courseDao.deleteAllByTable(tableId)
    }
}
