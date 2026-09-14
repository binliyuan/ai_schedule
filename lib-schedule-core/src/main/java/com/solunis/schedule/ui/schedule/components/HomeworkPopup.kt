package com.solunis.schedule.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.HomeworkBean
import com.solunis.schedule.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkPopup(
    course: CourseBean?,
    homework: List<HomeworkBean>,
    onDismiss: () -> Unit,
    onSaveEdit: (String, String) -> Unit,
    onAddHomework: (String) -> Unit,
    onToggleHomework: (HomeworkBean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (course == null) return

    var editName by remember(course.id) { mutableStateOf(course.courseName) }
    var editRoom by remember(course.id, course.room) { mutableStateOf(course.room) }
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        var hwText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加作业") },
            text = {
                OutlinedTextField(
                    value = hwText,
                    onValueChange = { hwText = it },
                    placeholder = { Text("输入作业内容") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (hwText.isNotBlank()) {
                        onAddHomework(hwText.trim())
                        showAddDialog = false
                    }
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = course.courseName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Text900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = course.room,
                    fontSize = 12.sp,
                    color = Text400
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color(0xFFEDE8F0)
            )

            // Edit section
            Text(
                text = "编辑课程",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Text400,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text("名称", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Text400, modifier = Modifier.width(32.dp))
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Text900),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0DCE8),
                        focusedBorderColor = Purple400
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text("教室", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Text400, modifier = Modifier.width(32.dp))
                OutlinedTextField(
                    value = editRoom,
                    onValueChange = { editRoom = it },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Text900),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0DCE8),
                        focusedBorderColor = Purple400
                    )
                )
            }

            Button(
                onClick = { onSaveEdit(editName.trim(), editRoom.trim()) },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Purple600, Pink500)),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("保存修改", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color(0xFFEDE8F0)
            )

            // Homework section
            Text(
                text = "作业清单",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Text400,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val courseHomework = homework.filter { it.courseId == course.id }

            if (courseHomework.isEmpty()) {
                Text(
                    text = "暂无作业",
                    fontSize = 12.sp,
                    color = Text400,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            } else {
                courseHomework.forEach { hw ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8F5FA))
                            .clickable { onToggleHomework(hw) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (hw.done) HwDotDone else Color.White)
                                .border(
                                    1.5.dp,
                                    if (hw.done) HwDotDone else Color(0xFFCCC8D4),
                                    CircleShape
                                )
                        ) {
                            if (hw.done) {
                                Text("✓", fontSize = 10.sp, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = hw.text,
                            fontSize = 12.sp,
                            color = if (hw.done) Text400 else Text900,
                            textDecoration = if (hw.done) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Purple200),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Purple500
                )
            ) {
                Text("＋ 添加作业", fontSize = 12.sp)
            }
        }
    }
}
