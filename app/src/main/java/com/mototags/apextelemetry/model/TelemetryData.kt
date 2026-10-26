package com.mototags.apextelemetry.model

import com.mototags.apextelemetry.ui.theme.AppTheme

data class TelemetryData(
    val rideState: RideState = RideState.READY,
    val selectedTheme: AppTheme = AppTheme.MOTOGP_CYAN,
    val currentLeanAngle: Float = 0f,
    val maxLeftLean: Float = 0f,
    val maxRightLean: Float = 0f,
    val lateralG: Float = 0f,
    val longitudinalG: Float = 0f,
    val maxLateralG: Float = 0f,
    val maxAccelG: Float = 0f,
    val maxBrakeG: Float = 0f,
    val speedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val avgSpeedKmh: Float = 0f,
    val rideDurationSeconds: Long = 0L,
    val isCalibrated: Boolean = false,
    val displayRotation: Int = 0,
    val activeTab: Int = 0 // 0: Cockpit HUD, 1: Saved Rides History
)
