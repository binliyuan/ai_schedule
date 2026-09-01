package com.solunis.schedule.data.repository

import androidx.lifecycle.LiveData
import com.solunis.schedule.data.database.dao.HomeworkDao
import com.solunis.schedule.data.database.entity.HomeworkBean

class HomeworkRepository(private val homeworkDao: HomeworkDao) {

    fun getHomeworkByCourse(courseId: Int, tableId: Int): LiveData<List<HomeworkBean>> =
        homeworkDao.getHomeworkByCourse(courseId, tableId)

    suspend fun getHomeworkByCourseSync(courseId: Int, tableId: Int): List<HomeworkBean> =
        homeworkDao.getHomeworkByCourseSync(courseId, tableId)

    fun getAllHomework(tableId: Int): LiveData<List<HomeworkBean>> =
        homeworkDao.getAllHomework(tableId)

    suspend fun insertHomework(homework: HomeworkBean): Long =
        homeworkDao.insertHomework(homework)

    suspend fun updateHomework(homework: HomeworkBean) =
        homeworkDao.updateHomework(homework)

    suspend fun deleteHomework(homework: HomeworkBean) =
        homeworkDao.deleteHomework(homework)
}
