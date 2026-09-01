package com.suda.yzune.wakeupschedule.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "timedetail",
    primaryKeys = ["node", "timeTable"],
    foreignKeys = [ForeignKey(
        entity = TimeTableBean::class,
        parentColumns = ["id"],
        childColumns = ["timeTable"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("timeTable")]
)
data class TimeDetailBean(
    val node: Int,
    val startTime: String,
    val endTime: String,
    val timeTable: Int
)
