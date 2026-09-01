package com.solunis.schedule.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.ui.schedule.components.*
import com.solunis.schedule.ui.theme.*

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val table by viewModel.currentTable.observeAsState()
    val courses by viewModel.allCourses.observeAsState(emptyList())
    val homework by viewModel.allHomework.observeAsState(emptyList())
    val timeDetails by viewModel.timeDetails.observeAsState(emptyList())
    val currentWeek by viewModel.currentWeek.observeAsState(1)
    val todayDow by viewModel.todayDayOfWeek.observeAsState(1)
    val selectedCourse by viewModel.selectedCourse.observeAsState()
    val showOverlay by viewModel.showDetailOverlay.observeAsState(false)
    val showHomework by viewModel.showHomeworkPopup.observeAsState(false)
    val syncState by viewModel.syncState.observeAsState(SyncState.IDLE)

    LaunchedEffect(table) {
        table?.let { viewModel.syncScheduleData() }
    }

    val currentCourse = viewModel.getCurrentCourse(courses, timeDetails)

    Box(modifier = modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(BgGrad1, BgGrad2, BgGrad3),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(600f, 1200f)
                    )
                )
        )

        // Decorative blobs
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 130.dp, y = (-120).dp)
                .clip(CircleShape)
                .background(Purple400.copy(alpha = 0.35f))
                .blur(50.dp)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-90).dp, y = 160.dp)
                .clip(CircleShape)
                .background(Pink500.copy(alpha = 0.25f))
                .blur(50.dp)
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 170.dp, y = 640.dp)
                .clip(CircleShape)
                .background(Purple600.copy(alpha = 0.2f))
                .blur(50.dp)
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            ScheduleHeader(
                dateText = viewModel.getFormattedDate(),
                weekNumber = currentWeek,
                dayOfWeekName = viewModel.getDayOfWeekName(),
                todayCourseCount = viewModel.getTodayCoursesCount(courses)
            )

            // Sync state indicator
            if (syncState == SyncState.LOADING) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = Purple500
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("正在同步课表...", fontSize = 12.sp, color = Text400)
                }
            } else if (syncState == SyncState.SUCCESS) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("课表已同步", fontSize = 12.sp, color = Text400)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current class card
            CurrentClassCard(
                course = currentCourse ?: courses.firstOrNull(),
                timeDetails = timeDetails,
                onLongPress = {
                    viewModel.selectCourse(currentCourse ?: courses.firstOrNull())
                    viewModel.showDetailOverlay(true)
                }
            )

            // Long press hint
            Text(
                text = "长按查看详细倒计时 →",
                fontSize = 11.sp,
                color = Purple500.copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 28.dp, top = 6.dp, bottom = 2.dp)
                    .wrapContentWidth(Alignment.End)
            )

            // Day selector
            DaySelector(todayDayOfWeek = todayDow)

            Spacer(modifier = Modifier.height(4.dp))

            // Schedule grid
            ScheduleGrid(
                courses = courses,
                homework = homework,
                nodes = table?.nodes ?: 12,
                currentWeek = currentWeek,
                todayDayOfWeek = todayDow,
                onCourseClick = { course ->
                    viewModel.selectCourse(course)
                    viewModel.showHomeworkPopup(true)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Detail overlay (long-press)
        CourseDetailOverlay(
            visible = showOverlay,
            course = selectedCourse ?: currentCourse ?: courses.firstOrNull(),
            timeDetails = timeDetails,
            courses = courses,
            currentWeek = currentWeek,
            onDismiss = { viewModel.showDetailOverlay(false) }
        )

        // Homework popup (course tap)
        if (showHomework && selectedCourse != null) {
            HomeworkPopup(
                course = selectedCourse,
                homework = homework,
                onDismiss = { viewModel.showHomeworkPopup(false) },
                onSaveEdit = { name, room ->
                    val c = selectedCourse!!
                    viewModel.updateCourseName(c.id, c.tableId, name)
                    viewModel.updateCourseRoom(c.id, c.tableId, c.day, c.startNode, room)
                },
                onAddHomework = { text ->
                    val c = selectedCourse!!
                    viewModel.addHomework(c.id, c.tableId, text)
                },
                onToggleHomework = { hw ->
                    viewModel.toggleHomework(hw)
                }
            )
        }
    }
}
