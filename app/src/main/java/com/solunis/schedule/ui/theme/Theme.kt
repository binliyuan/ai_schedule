package com.solunis.schedule.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Purple600,
    onPrimary = Color.White,
    primaryContainer = Purple200,
    onPrimaryContainer = Purple600,
    secondary = Pink500,
    onSecondary = Color.White,
    secondaryContainer = Pink200,
    onSecondaryContainer = Pink500,
    background = BgGrad1,
    onBackground = Text900,
    surface = Color.White,
    onSurface = Text900,
    surfaceVariant = Color(0xFFF5F0FA),
    onSurfaceVariant = Text600,
    outline = Purple200
)

@Composable
fun WakeupScheduleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = WakeupTypography,
        content = content
    )
}
