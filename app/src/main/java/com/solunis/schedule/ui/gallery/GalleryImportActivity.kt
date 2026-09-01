package com.solunis.schedule.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solunis.schedule.ui.theme.*

class GalleryImportActivity : AppCompatActivity() {

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            Toast.makeText(this, "已选择图片: $uri\n识别功能开发中...", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WakeupScheduleTheme {
                GalleryImportScreen(
                    onBack = { finish() },
                    onPickImage = { pickImageLauncher.launch("image/*") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryImportScreen(
    onBack: () -> Unit,
    onPickImage: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("图片导入", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgGrad1)
            )
        },
        containerColor = BgGrad1
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFFF8A65)))
                    )
            ) {
                Text("🖼️", fontSize = 48.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "从相册导入",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Text900
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "从相册选择课表截图\n自动解析课程信息并导入",
                fontSize = 14.sp,
                color = Text400,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onPickImage,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFFF8A65))),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Text("选择图片", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}
