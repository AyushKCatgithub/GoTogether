package com.quantax.gotogether.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Brand Colors (Premium & Vibrant) ───
// A highly vibrant, energetic primary gradient concept represented in solid form for the theme.
val GoTogetherOrange = Color(0xFFE8632B)
val GoTogetherOrangeDark = Color(0xFFC44D1A)
val GoTogetherOrangeLight = Color(0xFFFF8A50)
val VerifiedCyan = Color(0xFF00E5FF)
val WarningYellow = Color(0xFFFFB74D)
val AccentBlue = Color(0xFF4A90D9)
val AccentPurple = Color(0xFF7C4DFF)
val AccentGreen = Color(0xFF4CAF50)

// ─── Legacy direct-access colors (kept for backward compatibility during migration) ───
val DarkBackground = Color(0xFF05050A)
val DarkSurface = Color(0xFF101018)
val DarkSurfaceLight = Color(0xFF1A1A26)
val DarkNavBar = Color(0xFF05050A)
val CardBackground = Color(0xFF12121A)
val TextGray = Color(0xFF8A8A9E)
val TextLight = Color(0xFFD1D1DF)
val SoftWhite = Color(0xFFF4F4F8)

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

// ─── Dark Palette (OLED Black Base) ───
val DarkGoTogetherColors = GoTogetherColors(
    background = Color(0xFF05050A),
    surface = Color(0xFF101018),
    surfaceVariant = Color(0xFF1A1A26),
    navBar = Color(0xFF05050A).copy(alpha = 0.9f), // Translucent effect
    card = Color(0xFF12121A),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA0A0B0),
    textOnPrimary = Color.White,
    border = Color.White.copy(alpha = 0.05f),
    overlay = Color.Black.copy(alpha = 0.6f),
    divider = Color.White.copy(alpha = 0.04f),
    inputBackground = Color(0xFF161622),
    isDark = true
)

// ─── Light Palette (Crisp & Glassy) ───
val LightGoTogetherColors = GoTogetherColors(
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEF0F5),
    navBar = Color(0xFFFFFFFF).copy(alpha = 0.9f),
    card = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF111418),
    textSecondary = Color(0xFF6A6E83),
    textOnPrimary = Color.White,
    border = Color.Black.copy(alpha = 0.05f),
    overlay = Color.Black.copy(alpha = 0.3f),
    divider = Color.Black.copy(alpha = 0.04f),
    inputBackground = Color(0xFFF2F4F7),
    isDark = false
)

val LocalGoTogetherColors = staticCompositionLocalOf { DarkGoTogetherColors }