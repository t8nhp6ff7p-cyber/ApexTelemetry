package com.mototags.apextelemetry.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ApexTelemetryTheme(
    selectedTheme: AppTheme = AppTheme.MOTOGP_CYAN,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = selectedTheme.primaryColor,
        secondary = selectedTheme.secondaryColor,
        tertiary = selectedTheme.secondaryColor,
        background = Color.Black,
        surface = selectedTheme.surfaceColor,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onTertiary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
