package com.suda.yzune.wakeupschedule.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.suda.yzune.wakeupschedule.data.database.entity.TimeDetailBean
import com.suda.yzune.wakeupschedule.data.database.entity.TimeTableBean

@Dao
interface TimeDao {

    @Query("SELECT * FROM timetable")
    fun getAllTimeTables(): LiveData<List<TimeTableBean>>

    @Query("SELECT * FROM timetable WHERE id = :id")
    suspend fun getTimeTableById(id: Int): TimeTableBean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeTable(timeTable: TimeTableBean): Long

    @Update
    suspend fun updateTimeTable(timeTable: TimeTableBean)

    @Delete
    suspend fun deleteTimeTable(timeTable: TimeTableBean)

    @Query("SELECT * FROM timedetail WHERE timeTable = :timeTableId ORDER BY node")
    fun getTimeDetails(timeTableId: Int): LiveData<List<TimeDetailBean>>

    @Query("SELECT * FROM timedetail WHERE timeTable = :timeTableId ORDER BY node")
    suspend fun getTimeDetailsSync(timeTableId: Int): List<TimeDetailBean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeDetail(timeDetail: TimeDetailBean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeDetails(timeDetails: List<TimeDetailBean>)

    @Query("DELETE FROM timedetail WHERE timeTable = :timeTableId")
    suspend fun deleteTimeDetails(timeTableId: Int)
}
