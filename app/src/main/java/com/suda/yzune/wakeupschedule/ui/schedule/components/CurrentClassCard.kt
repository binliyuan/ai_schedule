package com.suda.yzune.wakeupschedule.ui.schedule.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suda.yzune.wakeupschedule.data.database.entity.CourseBean
import com.suda.yzune.wakeupschedule.data.database.entity.TimeDetailBean
import com.suda.yzune.wakeupschedule.ui.theme.*

@Composable
fun CurrentClassCard(
    course: CourseBean?,
    timeDetails: List<TimeDetailBean>,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (course == null) return

    val startTime = timeDetails.find { it.node == course.startNode }
    val endNode = course.startNode + course.step - 1
    val endTime = timeDetails.find { it.node == endNode }

    val timeText = if (startTime != null && endTime != null) {
        "${startTime.startTime} — ${endTime.endTime}  第 ${course.startNode}-${endNode} 节"
    } else ""

    val remainingMin = if (endTime != null) {
        val now = java.time.LocalTime.now()
        val end = java.time.LocalTime.parse(endTime.endTime)
        val diff = java.time.Duration.between(now, end).toMinutes()
        diff.coerceAtLeast(0).toInt()
    } else 23

    val progress = if (startTime != null && endTime != null) {
        val now = java.time.LocalTime.now()
        val start = java.time.LocalTime.parse(startTime.startTime)
        val end = java.time.LocalTime.parse(endTime.endTime)
        val total = java.time.Duration.between(start, end).toMinutes().toFloat()
        val elapsed = java.time.Duration.between(start, now).toMinutes().toFloat()
        if (total > 0) (elapsed / total).coerceIn(0f, 1f) else 0.62f
    } else 0.62f

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(CardGradStart, CardGradMid, CardGradEnd),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 500f)
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() }
                )
            }
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ring timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp)
            ) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    val strokeWidth = 5.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(radius * 2, radius * 2)

                    drawArc(
                        color = Color.White.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.9f),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("剩余", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        text = "$remainingMin",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text("min", fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "📍 ${course.room}",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "🕐 $timeText",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Status pill
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "进行中",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
