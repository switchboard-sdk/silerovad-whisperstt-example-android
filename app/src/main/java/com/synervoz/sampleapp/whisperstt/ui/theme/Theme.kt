package com.synervoz.sampleapp.whisperstt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = GreenNormal40,
    secondary = GreenGrey40,
    tertiary = GreenDark40,
    onPrimary = Color.White,
    background = Color(0xFF1C1C1E)
)

@Composable
fun WhisperSTTTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}