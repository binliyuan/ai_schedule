package com.solunis.schedule.data.network.dto

import com.solunis.schedule.data.database.entity.CourseBaseBean
import com.solunis.schedule.data.database.entity.CourseDetailBean

fun CourseDto.toBaseBean(tableId: Int) = CourseBaseBean(
    id = id,
    courseName = name,
    color = color,
    tableId = tableId
)

fun CourseDto.toDetailBean(tableId: Int) = CourseDetailBean(
    id = id,
    day = day,
    room = room,
    teacher = teacher,
    startNode = startNode,
    step = step,
    startWeek = startWeek,
    endWeek = endWeek,
    type = type,
    tableId = tableId
)
