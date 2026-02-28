package com.quantax.gotogether.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Brand Colors (constant across themes) ───
val GoTogetherOrange = Color(0xFFE8632B)
val GoTogetherOrangeDark = Color(0xFFC44D1A)
val GoTogetherOrangeLight = Color(0xFFFF8A50)
val VerifiedCyan = Color(0xFF00E5FF)
val WarningYellow = Color(0xFFFFB74D)
val AccentBlue = Color(0xFF4A90D9)
val AccentPurple = Color(0xFF7C4DFF)
val AccentGreen = Color(0xFF4CAF50)

// ─── Legacy direct-access colors (kept for backward compatibility during migration) ───
val DarkBackground = Color(0xFF0D0D1A)
val DarkSurface = Color(0xFF1A1A2E)
val DarkSurfaceLight = Color(0xFF222244)
val DarkNavBar = Color(0xFF0A0A14)
val CardBackground = Color(0xFF16162A)
val TextGray = Color(0xFF8E8EA0)
val TextLight = Color(0xFFCCCCDD)
val SoftWhite = Color(0xFFF0F0F5)

// ─── Semantic Color System ───

@Immutable
data class GoTogetherColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val navBar: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textOnPrimary: Color,
    val border: Color,
    val overlay: Color,
    val divider: Color,
    val inputBackground: Color,
    val isDark: Boolean
)

// ─── Dark Palette ───
val DarkGoTogetherColors = GoTogetherColors(
    background = Color(0xFF0D0D1A),
    surface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFF222244),
    navBar = Color(0xFF0A0A14),
    card = Color(0xFF16162A),
    textPrimary = Color(0xFFEEEEF2),
    textSecondary = Color(0xFF8E8EA0),
    textOnPrimary = Color.White,
    border = Color.White.copy(alpha = 0.08f),
    overlay = Color.Black.copy(alpha = 0.5f),
    divider = Color.White.copy(alpha = 0.06f),
    inputBackground = Color(0xFF16162A),
    isDark = true
)

// ─── Light Palette ───
val LightGoTogetherColors = GoTogetherColors(
    background = Color(0xFFF5F5FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEEEF4),
    navBar = Color(0xFFFFFFFF),
    card = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF1A1A2E),
    textSecondary = Color(0xFF6E6E82),
    textOnPrimary = Color.White,
    border = Color.Black.copy(alpha = 0.08f),
    overlay = Color.Black.copy(alpha = 0.25f),
    divider = Color.Black.copy(alpha = 0.06f),
    inputBackground = Color(0xFFF0F0F5),
    isDark = false
)

val LocalGoTogetherColors = staticCompositionLocalOf { DarkGoTogetherColors }