package com.mototags.apextelemetry.model

data class RideSession(
    val id: String = System.currentTimeMillis().toString(),
    val title: String = "Ride Session",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0L,
    val maxLeftLean: Float = 0f,
    val maxRightLean: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val avgSpeedKmh: Float = 0f,
    val maxLateralG: Float = 0f,
    val maxBrakeG: Float = 0f,
    val totalCornersLogged: Int = 0
)

enum class RideState {
    READY,      // Pre-ride: bike mounted, calibration prompt, start ride button
    RIDING,     // Active ride: sensors active, logging telemetry, recording peaks
    PAUSED,     // Ride temporarily paused (e.g. stopped at fuel station)
    SUMMARY     // Ride completed: summary sheet displayed with save/discard option
}
