package com.suda.yzune.wakeupschedule.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homework")
data class HomeworkBean(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val courseId: Int,
    val tableId: Int,
    val text: String,
    val done: Boolean = false
)
