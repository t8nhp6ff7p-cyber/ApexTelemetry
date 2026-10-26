package com.mototags.apextelemetry.model

enum class GpsQuality(val label: String, val colorHex: Long) {
    EXCELLENT("GPS: EXCELLENT", 0xFF00E676), // < 5m
    GOOD("GPS: GOOD", 0xFF00E5FF),          // 5m - 12m
    FAIR("GPS: FAIR", 0xFFFFD600),          // 12m - 25m
    WEAK("GPS: WEAK", 0xFFFF6D00),          // > 25m
    NO_LOCK("GPS: SEARCHING...", 0xFFFF1744)
}

data class SensorHealth(
    val gpsAccuracyMeters: Float = -1f,
    val gpsQuality: GpsQuality = GpsQuality.NO_LOCK,
    val sensorRateHz: Int = 0,
    val isGyroAvailable: Boolean = true,
    val isRotationVectorAvailable: Boolean = true,
    val mountVibrationLevel: Float = 0f, // 0..1 scale (detects loose phone mount)
    val totalDistanceKm: Float = 0f
)
