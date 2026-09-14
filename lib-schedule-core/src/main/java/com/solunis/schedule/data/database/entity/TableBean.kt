package com.solunis.schedule.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "table_bean",
    foreignKeys = [ForeignKey(
        entity = TimeTableBean::class,
        parentColumns = ["id"],
        childColumns = ["timeTable"],
        onDelete = ForeignKey.SET_DEFAULT
    )],
    indices = [Index("timeTable")]
)
data class TableBean(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tableName: String = "默认课表",
    val nodes: Int = 12,
    val background: String = "",
    val startDate: String = "",
    val maxWeek: Int = 20,
    val itemHeight: Int = 56,
    val itemAlpha: Int = 80,
    val strokeColor: Int = 0,
    val textColor: Int = -0x1000000,
    val courseTextColor: Int = -0x1,
    val showSat: Boolean = true,
    val showSun: Boolean = true,
    val sundayFirst: Boolean = false,
    val showOtherWeekCourse: Boolean = true,
    val showTime: Boolean = false,
    val type: Int = 0,
    val timeTable: Int = 1
)
