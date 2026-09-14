package com.solunis.schedule.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.ui.theme.*

@Composable
fun CourseBlock(
    course: CourseBean,
    cellHeight: Dp,
    isActive: Boolean,
    hasHomework: Boolean,
    allHomeworkDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorIndex = try {
        course.color.toInt()
    } catch (e: NumberFormatException) {
        course.id % courseColors.size
    }
    val colors = courseColors[colorIndex % courseColors.size]
    val blockHeight = cellHeight * course.step

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(blockHeight - 2.dp)
            .padding(horizontal = 2.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = colors.text.copy(alpha = 0.15f),
                spotColor = colors.text.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(colors.background)
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = if (isActive) Purple500 else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(start = 3.dp)
    ) {
        // Left border accent
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(colors.border.copy(alpha = 0.7f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, top = 5.dp, end = 5.dp, bottom = 4.dp)
        ) {
            Text(
                text = course.courseName,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = course.room,
                fontSize = 9.sp,
                color = colors.text.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Homework dot badge
        if (hasHomework) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (allHomeworkDone) HwDotDone else HwDotUndone)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 60)
@Composable
private fun CourseBlockPreview() {
    WakeupScheduleTheme {
        CourseBlock(
            course = CourseBean(
                id = 1, courseName = "高等数学", color = "0", tableId = 1,
                day = 1, room = "A301", teacher = "",
                startNode = 1, step = 2, startWeek = 1, endWeek = 20, type = 0
            ),
            cellHeight = 46.dp,
            isActive = true,
            hasHomework = true,
            allHomeworkDone = false,
            onClick = {}
        )
    }
}
