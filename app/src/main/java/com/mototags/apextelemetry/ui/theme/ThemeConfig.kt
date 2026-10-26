package com.mototags.apextelemetry.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val surfaceColor: Color,
    val cardColor: Color
) {
    MOTOGP_CYAN(
        title = "MotoGP Cyan",
        primaryColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFFFF6D00),
        surfaceColor = Color(0xFF0D1117),
        cardColor = Color(0xFF161B22)
    ),
    RACING_RED(
        title = "Ducati Red",
        primaryColor = Color(0xFFFF1744),
        secondaryColor = Color(0xFFFF9100),
        surfaceColor = Color(0xFF140B0D),
        cardColor = Color(0xFF221216)
    ),
    KTM_AMBER(
        title = "KTM Amber",
        primaryColor = Color(0xFFFF6D00),
        secondaryColor = Color(0xFFFFD600),
        surfaceColor = Color(0xFF161009),
        cardColor = Color(0xFF24190F)
    ),
    KAWASAKI_LIME(
        title = "Ninja Lime",
        primaryColor = Color(0xFF00E676),
        secondaryColor = Color(0xFF00B0FF),
        surfaceColor = Color(0xFF0A140F),
        cardColor = Color(0xFF12221A)
    ),
    MONOCHROME(
        title = "Stealth Sunlight",
        primaryColor = Color(0xFFFFFFFF),
        secondaryColor = Color(0xFFB0BEC5),
        surfaceColor = Color(0xFF121212),
        cardColor = Color(0xFF1E1E1E)
    )
}
