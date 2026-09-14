package com.solunis.schedule.ui.theme

import androidx.compose.ui.graphics.Color

// Primary brand colors (from design_preview oklch palette)
val Purple600 = Color(0xFF7C3AED)
val Purple500 = Color(0xFF8B5CF6)
val Purple400 = Color(0xFFA78BFA)
val Purple200 = Color(0xFFDDD6FE)
val Pink500 = Color(0xFFEC4899)
val Pink200 = Color(0xFFFBCFE8)

// Text colors
val Text900 = Color(0xFF1E1B2E)
val Text600 = Color(0xFF5B5675)
val Text400 = Color(0xFF9590A8)

// Background gradient
val BgGrad1 = Color(0xFFEDE8F5)
val BgGrad2 = Color(0xFFF2EDF5)
val BgGrad3 = Color(0xFFF5EBF0)

// Current class card gradient
val CardGradStart = Color(0xFF4C1D95)
val CardGradMid = Color(0xFF7C3AED)
val CardGradEnd = Color(0xFFE8368F)

// Course block color themes (7 color sets: background, text, border)
data class CourseColor(
    val background: Color,
    val text: Color,
    val border: Color
)

val courseColors = listOf(
    // c0 - purple
    CourseColor(Color(0xFFE8E0F8), Color(0xFF5B2DA6), Color(0xFF7C4DCC)),
    // c1 - pink
    CourseColor(Color(0xFFF5E0EA), Color(0xFF992D5B), Color(0xFFCC5480)),
    // c2 - green
    CourseColor(Color(0xFFE0F5E8), Color(0xFF1D6B3A), Color(0xFF40996D)),
    // c3 - amber
    CourseColor(Color(0xFFF5F0DC), Color(0xFF6B5200), Color(0xFF998020)),
    // c4 - blue
    CourseColor(Color(0xFFDEE8F5), Color(0xFF1A4D80), Color(0xFF407AB3)),
    // c5 - orange
    CourseColor(Color(0xFFF5E8D8), Color(0xFF804200), Color(0xFFB36820)),
    // c6 - magenta
    CourseColor(Color(0xFFF0E0F5), Color(0xFF6B2490), Color(0xFF9940C0))
)

// Overlay
val OverlayBg = Color(0xB8201030)

// Utility
val HwDotUndone = Color(0xFFE05252)
val HwDotDone = Color(0xFF4CAF50)
val White88 = Color(0xE0FFFFFF)
val GridLine = Color(0x2E9590A8)
