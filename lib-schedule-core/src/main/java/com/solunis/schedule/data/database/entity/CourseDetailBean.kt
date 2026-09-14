package com.solunis.schedule.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "coursedetail",
    primaryKeys = ["id", "tableId", "day", "startNode", "startWeek", "type"],
    foreignKeys = [ForeignKey(
        entity = CourseBaseBean::class,
        parentColumns = ["id", "tableId"],
        childColumns = ["id", "tableId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("id", "tableId")]
)
data class CourseDetailBean(
    val id: Int,
    val day: Int,
    val room: String = "",
    val teacher: String = "",
    val startNode: Int,
    val step: Int = 2,
    val startWeek: Int = 1,
    val endWeek: Int = 20,
    val type: Int = 0,
    val tableId: Int
)
