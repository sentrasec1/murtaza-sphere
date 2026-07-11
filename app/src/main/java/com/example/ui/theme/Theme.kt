package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

private val LightSphereColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    secondary = Color(0xFF0099CC),
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    error = CoralError,
    onError = Color.White
)

@Composable
fun EduQuestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightSphereColorScheme,
        content = content
    )
}
