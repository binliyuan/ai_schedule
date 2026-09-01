package com.solunis.schedule.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "coursebase",
    primaryKeys = ["id", "tableId"],
    foreignKeys = [ForeignKey(
        entity = TableBean::class,
        parentColumns = ["id"],
        childColumns = ["tableId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("tableId")]
)
data class CourseBaseBean(
    val id: Int,
    val courseName: String,
    val color: String = "",
    val tableId: Int
)
