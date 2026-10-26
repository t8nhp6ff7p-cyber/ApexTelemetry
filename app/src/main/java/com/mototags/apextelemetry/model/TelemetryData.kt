package com.mototags.apextelemetry.model

data class TelemetryData(
    val currentLeanAngle: Float = 0f,    // In degrees: - is Left, + is Right
    val maxLeftLean: Float = 0f,        // Absolute degrees (0..65+)
    val maxRightLean: Float = 0f,       // Absolute degrees (0..65+)
    val lateralG: Float = 0f,           // Lateral acceleration in G (- is Left, + is Right)
    val longitudinalG: Float = 0f,      // + is Acceleration, - is Braking
    val maxLateralG: Float = 0f,        // Maximum cornering G reached
    val maxAccelG: Float = 0f,          // Maximum acceleration G reached
    val maxBrakeG: Float = 0f,          // Maximum braking G reached (absolute)
    val speedKmh: Float = 0f,           // GPS Speed in km/h
    val maxSpeedKmh: Float = 0f,        // Top speed in km/h
    val isCalibrated: Boolean = false,  // Has zero-tare been performed
    val displayRotation: Int = 0        // Surface.ROTATION_0, ROTATION_90, etc.
)
