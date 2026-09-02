package com.darkstar.wallora.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WalloraColors = darkColorScheme(
    primary = Color(0xFFB58CFF),
    onPrimary = Color(0xFF24103F),
    primaryContainer = Color(0xFF3A2163),
    onPrimaryContainer = Color(0xFFEADBFF),
    background = Color(0xFF0D0B10),
    surface = Color(0xFF15121A),
    surfaceVariant = Color(0xFF211B2C),
    onBackground = Color(0xFFF7F2FA),
    onSurface = Color(0xFFF7F2FA),
    onSurfaceVariant = Color(0xFFB9AFBF),
)

@Composable
fun WalloraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WalloraColors,
        content = content,
    )
}
