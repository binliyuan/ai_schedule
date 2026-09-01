package com.solunis.schedule.ui.manage

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.database.entity.CourseBaseBean
import com.solunis.schedule.data.database.entity.CourseBean
import com.solunis.schedule.data.database.entity.CourseDetailBean
import com.solunis.schedule.data.repository.CourseRepository
import com.solunis.schedule.ui.theme.*

class CourseManageActivity : AppCompatActivity() {

    private lateinit var viewModel: CourseManageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val repo = CourseRepository(db.courseDao())
        viewModel = ViewModelProvider(
            this,
            CourseManageViewModelFactory(application, repo, db)
        )[CourseManageViewModel::class.java]

        setContent {
            WakeupScheduleTheme {
                CourseManageScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseManageScreen(
    viewModel: CourseManageViewModel,
    onBack: () -> Unit
) {
    val courses by viewModel.courses.observeAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddCourseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, room, day, startNode, step ->
                viewModel.addCourse(name, room, day, startNode, step)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("课程管理", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgGrad1
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Purple600,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加课程")
            }
        },
        containerColor = BgGrad1
    ) { padding ->
        if (courses.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                Text("暂无课程数据", color = Text400, fontSize = 16.sp)
            }
        } else {
            val grouped = courses.groupBy { it.id }
            LazyColumn(
                modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(grouped.entries.toList(), key = { it.key }) { (id, courseList) ->
                    val course = courseList.first()
                    val colorIdx = try { course.color.toInt() } catch (e: Exception) { id % courseColors.size }
                    val cc = courseColors[colorIdx % courseColors.size]

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp, 40.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(cc.border)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(course.courseName, fontWeight = FontWeight.SemiBold, color = Text900, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            val rooms = courseList.map { "周${it.day} 第${it.startNode}-${it.startNode + it.step - 1}节 ${it.room}" }
                            rooms.forEach { detail ->
                                Text(detail, fontSize = 12.sp, color = Text400)
                            }
                        }
                        IconButton(onClick = { viewModel.deleteCourse(id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFFE05252))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCourseDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, room: String, day: Int, startNode: Int, step: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("1") }
    var startNode by remember { mutableStateOf("1") }
    var step by remember { mutableStateOf("2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加课程") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("课程名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("教室") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = day, onValueChange = { day = it.filter { c -> c.isDigit() } }, label = { Text("星期") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = startNode, onValueChange = { startNode = it.filter { c -> c.isDigit() } }, label = { Text("开始节") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = step, onValueChange = { step = it.filter { c -> c.isDigit() } }, label = { Text("节数") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onAdd(name.trim(), room.trim(), day.toIntOrNull() ?: 1, startNode.toIntOrNull() ?: 1, step.toIntOrNull() ?: 2)
                }
            }) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
