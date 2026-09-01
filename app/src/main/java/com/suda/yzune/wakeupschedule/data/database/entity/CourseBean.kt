package com.suda.yzune.wakeupschedule.data.database.entity

data class CourseBean(
    val id: Int,
    val courseName: String,
    val color: String,
    val tableId: Int,
    val day: Int,
    val room: String,
    val teacher: String,
    val startNode: Int,
    val step: Int,
    val startWeek: Int,
    val endWeek: Int,
    val type: Int
)
