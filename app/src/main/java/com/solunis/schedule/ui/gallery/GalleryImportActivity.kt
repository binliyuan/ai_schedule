package com.solunis.schedule.ui.gallery

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
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
import com.solunis.schedule.data.ai.AiAgent
import com.solunis.schedule.data.ai.AiConfig
import com.solunis.schedule.data.ai.AiService
import com.solunis.schedule.data.ai.AiToolExecutor
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GalleryImportActivity : AppCompatActivity() {

    private var isProcessing = mutableStateOf(false)
    private var resultText = mutableStateOf("")

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            processImage(uri.toString())
        }
    }

    private fun processImage(uriString: String) {
        val config = AiConfig.getConfig(this)
        if (config == null) {
            Toast.makeText(this, "请先在设置中配置 AI 模型和 API Key", Toast.LENGTH_LONG).show()
            return
        }
        if (!config.supportsVision) {
            Toast.makeText(this, "${config.name} 不支持图片识别，请换用 DeepSeek 或豆包", Toast.LENGTH_LONG).show()
            return
        }

        isProcessing.value = true
        resultText.value = "正在识别课表..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uri = android.net.Uri.parse(uriString)
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                val db = AppDatabase.getDatabase(applicationContext)
                val agent = AiAgent(AiService(config), AiToolExecutor(db), config)
                val result = agent.recognizeScheduleFromImage(base64)

                withContext(Dispatchers.Main) {
                    isProcessing.value = false
                    if (result.success) {
                        resultText.value = result.message
                        Toast.makeText(this@GalleryImportActivity, "识别完成，已导入课表", Toast.LENGTH_LONG).show()
                    } else {
                        resultText.value = "识别失败: ${result.message}"
                        Toast.makeText(this@GalleryImportActivity, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isProcessing.value = false
                    resultText.value = "错误: ${e.message}"
                    Toast.makeText(this@GalleryImportActivity, "处理失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WakeupScheduleTheme {
                GalleryImportScreen(
                    onBack = { finish() },
                    onPickImage = { pickImageLauncher.launch("image/*") },
                    isProcessing = isProcessing.value,
                    resultText = resultText.value
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryImportScreen(
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    isProcessing: Boolean = false,
    resultText: String = ""
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
                text = "选择课表截图，AI 自动识别并导入课程",
                fontSize = 14.sp,
                color = Text400,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(32.dp))

            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFFFFB74D)
                )
                Spacer(Modifier.height(16.dp))
                Text(resultText, fontSize = 14.sp, color = Text400)
            } else {
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

                if (resultText.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = resultText,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp,
                            color = Text600,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
