package com.mototags.apextelemetry.model

data class TelemetryPoint(
    val timestamp: Long,
    val speedKmh: Float,
    val leanAngle: Float,
    val lateralG: Float,
    val longitudinalG: Float,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0
)

data class RideSession(
    val id: String = System.currentTimeMillis().toString(),
    val title: String = "Ride Session",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0L,
    val movingDurationSeconds: Long = 0L,
    val distanceKm: Float = 0f,
    val maxLeftLean: Float = 0f,
    val maxRightLean: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val avgSpeedKmh: Float = 0f,
    val maxLateralG: Float = 0f,
    val maxBrakeG: Float = 0f,
    val points: List<TelemetryPoint> = emptyList(),
    val isRecovered: Boolean = false
)

enum class RideState {
    READY,
    RIDING,
    PAUSED,
    SUMMARY
}
