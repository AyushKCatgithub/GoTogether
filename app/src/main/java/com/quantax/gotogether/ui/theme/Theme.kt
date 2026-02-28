package com.quantax.gotogether.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Material 3 Color Schemes (wired to brand) ───

private val DarkColorScheme = darkColorScheme(
    primary = GoTogetherOrange,
    secondary = GoTogetherOrangeLight,
    tertiary = AccentBlue,
    background = Color(0xFF0D0D1A),
    surface = Color(0xFF1A1A2E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFEEEEF2),
    onSurface = Color(0xFFEEEEF2)
)

private val LightColorScheme = lightColorScheme(
    primary = GoTogetherOrange,
    secondary = GoTogetherOrangeDark,
    tertiary = AccentBlue,
    background = Color(0xFFF5F5FA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E)
)

// ─── Theme Composable ───

@Composable
fun GoTogetherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val goTogetherColors = if (darkTheme) DarkGoTogetherColors else LightGoTogetherColors

    // Status bar icon color adapts to theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalGoTogetherColors provides goTogetherColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// ─── Convenience accessor ───
object GoTogetherTheme {
    val colors: GoTogetherColors
        @Composable
        get() = LocalGoTogetherColors.current
}