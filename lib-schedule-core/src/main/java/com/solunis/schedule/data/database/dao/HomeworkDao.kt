package com.solunis.schedule.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.solunis.schedule.data.database.entity.HomeworkBean

@Dao
interface HomeworkDao {

    @Query("SELECT * FROM homework WHERE courseId = :courseId AND tableId = :tableId")
    fun getHomeworkByCourse(courseId: Int, tableId: Int): LiveData<List<HomeworkBean>>

    @Query("SELECT * FROM homework WHERE courseId = :courseId AND tableId = :tableId")
    suspend fun getHomeworkByCourseSync(courseId: Int, tableId: Int): List<HomeworkBean>

    @Query("SELECT * FROM homework WHERE tableId = :tableId")
    fun getAllHomework(tableId: Int): LiveData<List<HomeworkBean>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkBean): Long

    @Update
    suspend fun updateHomework(homework: HomeworkBean)

    @Delete
    suspend fun deleteHomework(homework: HomeworkBean)

    @Query("DELETE FROM homework WHERE courseId = :courseId AND tableId = :tableId")
    suspend fun deleteHomeworkByCourse(courseId: Int, tableId: Int)
}
