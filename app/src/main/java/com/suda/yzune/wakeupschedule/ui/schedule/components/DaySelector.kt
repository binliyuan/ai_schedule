package com.suda.yzune.wakeupschedule.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suda.yzune.wakeupschedule.ui.theme.*

@Composable
fun DaySelector(
    todayDayOfWeek: Int,
    modifier: Modifier = Modifier
) {
    val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(36.dp))

        dayNames.forEachIndexed { index, name ->
            val dayNum = index + 1
            val isToday = dayNum == todayDayOfWeek

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isToday) {
                                Modifier.background(
                                    Brush.linearGradient(
                                        colors = listOf(Purple600, Pink500)
                                    )
                                )
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isToday) Color.White else Text400
                    )
                }

                if (isToday) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Pink500)
                    )
                } else {
                    Spacer(modifier = Modifier.height(7.dp))
                }
            }
        }
    }
}
