package com.nepetis.box.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4FF),
    secondary = Color(0xFFFF00FF),
    tertiary = Color(0xFF00FF88),
    background = Color.Black,
    surface = Color(0xFF1A1A1A),
    error = Color(0xFFFF1493)
)

@Composable
fun NepetisBoxTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
