package com.solunis.schedule.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.solunis.schedule.data.local.BackgroundManager

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val bgFile = BackgroundManager.getBackgroundFile(context)

    Box(modifier = modifier.fillMaxSize()) {
        if (bgFile != null) {
            Image(
                painter = rememberAsyncImagePainter(bgFile),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.3f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BgGrad1, BgGrad2, BgGrad3),
                            start = Offset(0f, 0f),
                            end = Offset(600f, 1200f)
                        )
                    )
            )
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
        }

        content()
    }
}
