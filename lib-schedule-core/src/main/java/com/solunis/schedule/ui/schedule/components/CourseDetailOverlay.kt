package com.solunis.schedule.ui.schedule.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.TimeDetailBean
import com.solunis.schedule.ui.theme.*

@Composable
fun CourseDetailOverlay(
    visible: Boolean,
    course: CourseBean?,
    timeDetails: List<TimeDetailBean>,
    courses: List<CourseBean>,
    currentWeek: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && course != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (course == null) return@AnimatedVisibility

        val startTime = timeDetails.find { it.node == course.startNode }
        val endNode = course.startNode + course.step - 1
        val endTime = timeDetails.find { it.node == endNode }

        val remainingMin = if (endTime != null) {
            val now = java.time.LocalTime.now()
            val end = java.time.LocalTime.parse(endTime.endTime)
            java.time.Duration.between(now, end).toMinutes().coerceAtLeast(0).toInt()
        } else 23

        val progress = if (startTime != null && endTime != null) {
            val now = java.time.LocalTime.now()
            val start = java.time.LocalTime.parse(startTime.startTime)
            val end = java.time.LocalTime.parse(endTime.endTime)
            val total = java.time.Duration.between(start, end).toMinutes().toFloat()
            val elapsed = java.time.Duration.between(start, now).toMinutes().toFloat()
            if (total > 0) (elapsed / total).coerceIn(0f, 1f) else 0.65f
        } else 0.65f

        val nextCourse = courses.filter {
            it.day == course.day && it.startNode > endNode &&
                    it.startWeek <= currentWeek && it.endWeek >= currentWeek
        }.minByOrNull { it.startNode }

        val nextTimeText = nextCourse?.let { nc ->
            val t = timeDetails.find { it.node == nc.startNode }
            "${nc.courseName}  ${t?.startTime ?: ""}"
        } ?: "无"

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(OverlayBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Decorative blobs
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(x = (-80).dp, y = (-200).dp)
                    .clip(CircleShape)
                    .background(Purple500.copy(alpha = 0.5f))
                    .blur(60.dp)
            )
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .offset(x = 80.dp, y = 200.dp)
                    .clip(CircleShape)
                    .background(Pink500.copy(alpha = 0.4f))
                    .blur(60.dp)
            )

            // Detail card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 28.dp, vertical = 32.dp)
            ) {
                Text(
                    text = course.courseName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 28.dp)
                ) {
                    DetailTag("📍 ${course.room}")
                    if (course.teacher.isNotEmpty()) {
                        DetailTag("👨‍🏫 ${course.teacher}")
                    }
                }

                // Big ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(radius * 2, radius * 2)

                        // Background circle
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = size.minDimension / 2
                        )
                        // Track
                        drawArc(
                            color = Color.White.copy(alpha = 0.18f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress
                        drawArc(
                            brush = Brush.linearGradient(listOf(Purple400, Pink500)),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Inner content
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(164.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.07f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("剩余", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                text = "$remainingMin",
                                fontSize = 62.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text("分钟", fontSize = 16.sp, color = Color.White.copy(alpha = 0.75f))
                        }
                    }
                }

                // Time row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    TimeBox("开始时间", startTime?.startTime ?: "08:00")
                    Text("→", fontSize = 20.sp, color = Color.White.copy(alpha = 0.4f))
                    TimeBox("结束时间", endTime?.endTime ?: "09:40")
                }

                // Progress bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text("课程进度", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(Purple400, Pink500))
                                )
                        )
                    }
                }

                // Next class
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("⏭️", fontSize = 22.sp, modifier = Modifier.padding(end = 12.dp))
                    Column {
                        Text("下一节课", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                        Text(
                            text = nextTimeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "点击关闭",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
private fun DetailTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
private fun TimeBox(label: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 4.dp))
        Text(text = time, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
