package com.solunis.schedule.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.solunis.schedule.data.database.dao.CourseDao
import com.solunis.schedule.data.database.entity.CourseBaseBean
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.CourseDetailBean
import com.solunis.schedule.data.network.ApiService
import com.solunis.schedule.data.network.dto.toBaseBean
import com.solunis.schedule.data.network.dto.toDetailBean

class CourseRepository(
    private val courseDao: CourseDao,
    private val apiService: ApiService? = null
) {

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

    suspend fun syncFromNetwork(tableId: Int): Result<Int> {
        val api = apiService ?: return Result.failure(Exception("ApiService not configured"))
        return try {
            val response = api.syncSchedule()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val courseDtos = data.courses

                val baseSet = mutableSetOf<String>()
                courseDao.deleteAllByTable(tableId)

                courseDtos.forEach { dto ->
                    val key = "${dto.id}_${tableId}"
                    if (key !in baseSet) {
                        courseDao.insertCourseBase(dto.toBaseBean(tableId))
                        baseSet.add(key)
                    }
                    courseDao.insertCourseDetail(dto.toDetailBean(tableId))
                }

                Log.d("SyncSchedule", "Synced ${courseDtos.size} courses from network")
                Result.success(courseDtos.size)
            } else {
                Log.w("SyncSchedule", "Server returned ${response.code()}")
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.w("SyncSchedule", "Network sync failed", e)
            Result.failure(e)
        }
    }
}
