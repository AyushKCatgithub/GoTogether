package com.quantax.gotogether.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Brand Colors ───
// Orange is the single chromatic accent — reserved for CTAs and active/selected states.
val GoTogetherOrange = Color(0xFFE8632B)
val GoTogetherOrangeDark = Color(0xFFC44D1A)
val GoTogetherOrangeLight = Color(0xFFFF8A50)

// ─── Extended / semantic accents (not part of the core M3 roles) ───
val VerifiedCyan = Color(0xFF00B8D4)
val WarningYellow = Color(0xFFFFB74D)
val AccentBlue = Color(0xFF4A90D9)
val AccentPurple = Color(0xFF7C4DFF)
val AccentGreen = Color(0xFF3CA55C)

// ─── Legacy direct-access colors (kept for backward compatibility) ───
val DarkBackground = Color(0xFF100E14)
val DarkSurface = Color(0xFF1B1922)
val DarkSurfaceLight = Color(0xFF26242E)
val DarkNavBar = Color(0xFF100E14)
val CardBackground = Color(0xFF1B1922)
val TextGray = Color(0xFF8A8A9E)
val TextLight = Color(0xFFD1D1DF)
val SoftWhite = Color(0xFFF4F4F8)

// ═══════════════════════════════════════════════════════════════════
//  Material 3 color roles — neutral tonal surfaces + orange primary.
//  Referenced from Theme.kt to build the light/dark ColorSchemes.
// ═══════════════════════════════════════════════════════════════════

// ─── Light roles ───
val md_light_primary = GoTogetherOrange
val md_light_onPrimary = Color(0xFFFFFFFF)
val md_light_primaryContainer = Color(0xFFFFDBCC)
val md_light_onPrimaryContainer = Color(0xFF351000)
val md_light_secondary = Color(0xFF77574A)
val md_light_onSecondary = Color(0xFFFFFFFF)
val md_light_secondaryContainer = Color(0xFFFFDBCC)
val md_light_onSecondaryContainer = Color(0xFF2C160C)
val md_light_tertiary = AccentBlue
val md_light_onTertiary = Color(0xFFFFFFFF)
val md_light_tertiaryContainer = Color(0xFFD5E3FF)
val md_light_onTertiaryContainer = Color(0xFF001C38)
val md_light_error = Color(0xFFB3261E)
val md_light_onError = Color(0xFFFFFFFF)
val md_light_errorContainer = Color(0xFFF9DEDC)
val md_light_onErrorContainer = Color(0xFF410E0B)
val md_light_background = Color(0xFFF7F8FA)
val md_light_onBackground = Color(0xFF111418)
val md_light_surface = Color(0xFFFDFCFF)
val md_light_onSurface = Color(0xFF111418)
val md_light_surfaceVariant = Color(0xFFE7E1DE)
val md_light_onSurfaceVariant = Color(0xFF52443C)
val md_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_light_surfaceContainerLow = Color(0xFFF5F4F8)
val md_light_surfaceContainer = Color(0xFFEFEEF3)
val md_light_surfaceContainerHigh = Color(0xFFE9E8EE)
val md_light_surfaceContainerHighest = Color(0xFFE3E2E9)
val md_light_outline = Color(0xFF85736B)
val md_light_outlineVariant = Color(0xFFD7C2B8)
val md_light_scrim = Color(0xFF000000)
val md_light_inverseSurface = Color(0xFF2E2B29)
val md_light_inverseOnSurface = Color(0xFFF5EFEC)
val md_light_inversePrimary = Color(0xFFFFB59B)

// ─── Dark roles ───
val md_dark_primary = GoTogetherOrange
val md_dark_onPrimary = Color(0xFFFFFFFF)
val md_dark_primaryContainer = Color(0xFF7A3312)
val md_dark_onPrimaryContainer = Color(0xFFFFDBCC)
val md_dark_secondary = Color(0xFFE7BDAC)
val md_dark_onSecondary = Color(0xFF442A1D)
val md_dark_secondaryContainer = Color(0xFF5D4033)
val md_dark_onSecondaryContainer = Color(0xFFFFDBCC)
val md_dark_tertiary = Color(0xFFA6C8FF)
val md_dark_onTertiary = Color(0xFF00325B)
val md_dark_tertiaryContainer = Color(0xFF214A73)
val md_dark_onTertiaryContainer = Color(0xFFD5E3FF)
val md_dark_error = Color(0xFFF2B8B5)
val md_dark_onError = Color(0xFF601410)
val md_dark_errorContainer = Color(0xFF8C1D18)
val md_dark_onErrorContainer = Color(0xFFF9DEDC)
val md_dark_background = Color(0xFF100E14)
val md_dark_onBackground = Color(0xFFE5E1E9)
val md_dark_surface = Color(0xFF100E14)
val md_dark_onSurface = Color(0xFFE5E1E9)
val md_dark_surfaceVariant = Color(0xFF45464F)
val md_dark_onSurfaceVariant = Color(0xFFC6C5D0)
val md_dark_surfaceContainerLowest = Color(0xFF0B0910)
val md_dark_surfaceContainerLow = Color(0xFF18161F)
val md_dark_surfaceContainer = Color(0xFF1C1A23)
val md_dark_surfaceContainerHigh = Color(0xFF26242E)
val md_dark_surfaceContainerHighest = Color(0xFF312F39)
val md_dark_outline = Color(0xFF90909A)
val md_dark_outlineVariant = Color(0xFF45464F)
val md_dark_scrim = Color(0xFF000000)
val md_dark_inverseSurface = Color(0xFFE5E1E9)
val md_dark_inverseOnSurface = Color(0xFF1C1A23)
val md_dark_inversePrimary = GoTogetherOrange

// ═══════════════════════════════════════════════════════════════════
//  GoTogetherColors bridge — semantic accessor kept for existing call
//  sites (~325 refs). Values now map onto the M3 tonal roles above so
//  every screen inherits consistent Material 3 surfaces automatically.
// ═══════════════════════════════════════════════════════════════════

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

val DarkGoTogetherColors = GoTogetherColors(
    background = md_dark_background,
    surface = md_dark_surface,
    surfaceVariant = md_dark_surfaceVariant,
    navBar = md_dark_surfaceContainer,
    card = md_dark_surfaceContainer,
    textPrimary = md_dark_onSurface,
    textSecondary = md_dark_onSurfaceVariant,
    textOnPrimary = Color.White,
    border = md_dark_outlineVariant,
    overlay = Color.Black.copy(alpha = 0.6f),
    divider = md_dark_outlineVariant,
    inputBackground = md_dark_surfaceContainerHigh,
    isDark = true
)

val LightGoTogetherColors = GoTogetherColors(
    background = md_light_background,
    surface = md_light_surface,
    surfaceVariant = md_light_surfaceVariant,
    navBar = md_light_surfaceContainer,
    card = md_light_surfaceContainerLowest,
    textPrimary = md_light_onSurface,
    textSecondary = md_light_onSurfaceVariant,
    textOnPrimary = Color.White,
    border = md_light_outlineVariant,
    overlay = Color.Black.copy(alpha = 0.3f),
    divider = md_light_outlineVariant,
    inputBackground = md_light_surfaceContainerHigh,
    isDark = false
)

val LocalGoTogetherColors = staticCompositionLocalOf { DarkGoTogetherColors }
