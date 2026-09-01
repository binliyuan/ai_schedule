package com.solunis.schedule.data.network.dto

import com.google.gson.annotations.SerializedName

data class ScheduleResponse(
    val courses: List<CourseDto>,
    val semester: String,
    val startDate: String,
    val maxWeek: Int
)

data class CourseDto(
    val id: Int,
    val name: String,
    val color: String,
    val day: Int,
    val room: String,
    val teacher: String,
    val startNode: Int,
    val step: Int,
    val startWeek: Int,
    val endWeek: Int,
    val type: Int
)
