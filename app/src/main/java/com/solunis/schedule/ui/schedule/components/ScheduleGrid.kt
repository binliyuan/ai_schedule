package com.solunis.schedule.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.HomeworkBean
import com.solunis.schedule.ui.theme.GridLine
import com.solunis.schedule.ui.theme.Text400

@Composable
fun ScheduleGrid(
    courses: List<CourseBean>,
    homework: List<HomeworkBean>,
    nodes: Int,
    currentWeek: Int,
    todayDayOfWeek: Int,
    cellHeight: Dp = 46.dp,
    onCourseClick: (CourseBean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        // Period labels column
        Column(modifier = Modifier.width(36.dp)) {
            for (i in 1..nodes) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(cellHeight)
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = GridLine,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1f
                            )
                        }
                ) {
                    Text(
                        text = "$i",
                        fontSize = 12.sp,
                        color = Text400
                    )
                }
            }
        }

        // 7 day columns
        for (day in 1..7) {
            val dayCourses = courses.filter { course ->
                course.day == day &&
                        course.startWeek <= currentWeek &&
                        course.endWeek >= currentWeek
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(cellHeight * nodes)
            ) {
                // Grid cell borders
                Column {
                    for (i in 1..nodes) {
                        Box(
                            modifier = Modifier
                                .height(cellHeight)
                                .fillMaxWidth()
                                .drawBehind {
                                    drawLine(
                                        color = GridLine,
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = 1f
                                    )
                                }
                        )
                    }
                }

                // Course blocks positioned absolutely
                dayCourses.forEach { course ->
                    val topOffset = cellHeight * (course.startNode - 1) + 1.dp
                    val isCurrentlyActive = run {
                        try {
                            course.day == todayDayOfWeek &&
                                    course.startWeek <= currentWeek &&
                                    course.endWeek >= currentWeek
                        } catch (e: Exception) {
                            false
                        }
                    }

                    val courseHomework = homework.filter { it.courseId == course.id }
                    val hasHomework = courseHomework.isNotEmpty()
                    val allDone = hasHomework && courseHomework.all { it.done }

                    Box(modifier = Modifier.offset(y = topOffset)) {
                        CourseBlock(
                            course = course,
                            cellHeight = cellHeight,
                            isActive = isCurrentlyActive,
                            hasHomework = hasHomework,
                            allHomeworkDone = allDone,
                            onClick = { onCourseClick(course) }
                        )
                    }
                }
            }
        }
    }
}
