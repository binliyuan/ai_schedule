package com.solunis.schedule.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.solunis.schedule.data.database.entity.CourseBaseBean
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.CourseDetailBean

@Dao
interface CourseDao {

    @Query("SELECT * FROM coursebase NATURAL JOIN coursedetail WHERE tableId = :tableId")
    fun getCoursesByTableId(tableId: Int): LiveData<List<CourseBean>>

    @Query("SELECT * FROM coursebase NATURAL JOIN coursedetail WHERE tableId = :tableId")
    suspend fun getCoursesByTableIdSync(tableId: Int): List<CourseBean>

    @Query("SELECT * FROM coursebase NATURAL JOIN coursedetail WHERE tableId = :tableId AND day = :day")
    fun getCoursesByDay(tableId: Int, day: Int): LiveData<List<CourseBean>>

    @Query("SELECT * FROM coursebase NATURAL JOIN coursedetail WHERE tableId = :tableId AND day = :day")
    suspend fun getCoursesByDaySync(tableId: Int, day: Int): List<CourseBean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseBase(courseBase: CourseBaseBean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseDetail(courseDetail: CourseDetailBean)

    @Update
    suspend fun updateCourseBase(courseBase: CourseBaseBean)

    @Update
    suspend fun updateCourseDetail(courseDetail: CourseDetailBean)

    @Query("DELETE FROM coursebase WHERE id = :id AND tableId = :tableId")
    suspend fun deleteCourseBase(id: Int, tableId: Int)

    @Query("DELETE FROM coursedetail WHERE id = :id AND tableId = :tableId")
    suspend fun deleteCourseDetail(id: Int, tableId: Int)

    @Query("DELETE FROM coursebase WHERE tableId = :tableId")
    suspend fun deleteAllByTable(tableId: Int)
}
