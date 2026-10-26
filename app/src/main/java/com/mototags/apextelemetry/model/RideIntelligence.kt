package com.mototags.apextelemetry.model

data class BrakingEvent(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val peakBrakeG: Float,
    val entrySpeedKmh: Float,
    val exitSpeedKmh: Float
)

data class CornerEvent(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val direction: String, // "LEFT" or "RIGHT"
    val peakLeanAngle: Float,
    val apexSpeedKmh: Float
)

data class RideIntelligence(
    val totalLeftCorners: Int = 0,
    val totalRightCorners: Int = 0,
    val elevationGainMeters: Float = 0f,
    val minAltitudeMeters: Double = 0.0,
    val maxAltitudeMeters: Double = 0.0,
    val smoothnessScore: Int = 85, // 0..100 scale
    val hardBrakingEvents: List<BrakingEvent> = emptyList(),
    val cornerEvents: List<CornerEvent> = emptyList()
)
