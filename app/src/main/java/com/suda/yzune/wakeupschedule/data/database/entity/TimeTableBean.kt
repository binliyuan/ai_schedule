package com.suda.yzune.wakeupschedule.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "timetable")
data class TimeTableBean(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "默认",
    val sameLen: Boolean = true,
    val courseLen: Int = 50
)
