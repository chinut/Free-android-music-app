// ============================================================
// 15. ui/theme/Theme.kt
// ============================================================
package com.example.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6B9FFF),
    onPrimary = Color.White,
    secondary = Color(0xFF8EC5FF),
    background = Color(0xFF121212),
    surface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFF262529),
    onSurface = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFFB0A89F),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4A90E2),
    onPrimary = Color.White,
    secondary = Color(0xFF6FA8FF),
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEDEFF3),
)

@Composable
fun MusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}