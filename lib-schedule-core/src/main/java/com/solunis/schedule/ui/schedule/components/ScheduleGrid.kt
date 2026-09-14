package com.solunis.schedule.ui.schedule.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.HomeworkBean
import com.solunis.schedule.data.database.entity.TimeDetailBean
import com.solunis.schedule.ui.theme.GridLine
import com.solunis.schedule.ui.theme.Text400

@Composable
fun ScheduleGrid(
    courses: List<CourseBean>,
    homework: List<HomeworkBean>,
    timeDetails: List<TimeDetailBean>,
    nodes: Int,
    currentWeek: Int,
    todayDayOfWeek: Int,
    cellHeight: Dp = 46.dp,
    onCourseClick: (CourseBean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        // Period labels column — show start time for each node
        Column(modifier = Modifier.width(36.dp)) {
            for (i in 1..nodes) {
                val timeLabel = timeDetails.find { it.node == i }?.startTime ?: "$i"
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
                        text = timeLabel,
                        fontSize = 10.sp,
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

@Preview(showBackground = true, backgroundColor = 0xFFEDE8F5, widthDp = 390, heightDp = 560)
@Composable
private fun ScheduleGridPreview() {
    val sampleCourses = listOf(
        CourseBean(1, "高等数学", "0", 1, 1, "A301", "", 1, 2, 1, 20, 0),
        CourseBean(2, "大学物理", "1", 1, 1, "B202", "", 5, 2, 1, 20, 0),
        CourseBean(3, "英语听力", "4", 1, 1, "C105", "", 9, 2, 1, 20, 0),
        CourseBean(4, "线性代数", "3", 1, 2, "A205", "", 3, 2, 1, 20, 0),
        CourseBean(5, "计算机", "4", 1, 2, "D401", "", 7, 2, 1, 20, 0),
        CourseBean(1, "高等数学", "0", 1, 3, "A301", "", 1, 2, 1, 20, 0),
        CourseBean(6, "程序设计", "2", 1, 3, "E302", "", 5, 3, 1, 20, 0),
        CourseBean(7, "体育", "5", 1, 3, "操场", "", 9, 2, 1, 20, 0),
        CourseBean(2, "大学物理", "1", 1, 4, "B202", "", 1, 2, 1, 20, 0),
        CourseBean(8, "思想政治", "6", 1, 4, "F101", "", 3, 2, 1, 20, 0),
        CourseBean(9, "英语写作", "4", 1, 4, "C203", "", 7, 2, 1, 20, 0),
        CourseBean(4, "线性代数", "3", 1, 5, "A205", "", 1, 2, 1, 20, 0),
        CourseBean(6, "程序设计", "2", 1, 5, "E302", "", 3, 2, 1, 20, 0),
        CourseBean(10, "实验物理", "1", 1, 5, "G201", "", 7, 3, 1, 20, 0),
        CourseBean(11, "摄影", "5", 1, 6, "H102", "", 3, 2, 1, 20, 0),
        CourseBean(12, "自习", "3", 1, 7, "图书馆", "", 5, 2, 1, 20, 0)
    )
    val sampleHomework = listOf(
        HomeworkBean(1, 1, 1, "完成课后习题", false),
        HomeworkBean(2, 6, 1, "提交实验报告", true)
    )
    val sampleTimeDetails = listOf(
        TimeDetailBean(1, "08:00", "08:50", 1),
        TimeDetailBean(2, "08:55", "09:45", 1),
        TimeDetailBean(3, "10:00", "10:50", 1),
        TimeDetailBean(4, "10:55", "11:45", 1),
        TimeDetailBean(5, "14:00", "14:50", 1),
        TimeDetailBean(6, "14:55", "15:45", 1),
        TimeDetailBean(7, "16:00", "16:50", 1),
        TimeDetailBean(8, "16:55", "17:45", 1),
        TimeDetailBean(9, "19:00", "19:50", 1),
        TimeDetailBean(10, "19:55", "20:45", 1),
        TimeDetailBean(11, "20:55", "21:45", 1),
        TimeDetailBean(12, "21:55", "22:45", 1)
    )
    com.solunis.schedule.ui.theme.WakeupScheduleTheme {
        ScheduleGrid(
            courses = sampleCourses,
            homework = sampleHomework,
            timeDetails = sampleTimeDetails,
            nodes = 12,
            currentWeek = 11,
            todayDayOfWeek = 6,
            onCourseClick = {
            }
        )
    }
}
