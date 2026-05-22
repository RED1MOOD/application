package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = QuranPrimary,
    secondary = QuranSecondary,
    tertiary = DynamicGold,
    background = QuranDarkBg,
    surface = QuranSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = PureWhite,
    onSurface = PureWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = QuranPrimary,
    secondary = QuranSecondary,
    tertiary = DynamicGold,
    background = Color(0xFFF9FAFB),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme by default for luxurious player look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

