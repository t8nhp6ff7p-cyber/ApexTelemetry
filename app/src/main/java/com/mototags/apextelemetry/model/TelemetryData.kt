package com.mototags.apextelemetry.model

import com.mototags.apextelemetry.ui.theme.AppTheme

enum class DisplayStyle(val title: String, val subtitle: String) {
    ANALOG_DIAL("Classic Analog", "Precision Needle Dial"),
    DIGITAL_MOTO("MotoGP Digital", "Segmented Arc Telemetry"),
    RALLY_TWIN("Rally Twin Pods", "Dual Gauge Binnacle"),
    HUD_MIRROR("HUD Windshield", "Flipped Night Projection")
}

enum class LeanScale(val maxDegrees: Int, val label: String) {
    STREET_45(45, "45° (Street / Cruiser)"),
    SPORT_55(55, "55° (Sport / Twisties)"),
    TRACK_65(65, "65° (Track / MotoGP)")
}

enum class SpeedUnit(val label: String, val factorToKmh: Float) {
    KMH("KM/H", 1.0f),
    MPH("MPH", 0.621371f)
}

data class TelemetryData(
    val rideState: RideState = RideState.READY,
    val selectedTheme: AppTheme = AppTheme.MOTOGP_CYAN,
    val displayStyle: DisplayStyle = DisplayStyle.ANALOG_DIAL,
    val leanScale: LeanScale = LeanScale.SPORT_55,
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val isInvertedDirection: Boolean = false,
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
    val tripDistanceKm: Float = 0f,
    val movingTimeSeconds: Long = 0L,
    val rideDurationSeconds: Long = 0L,
    val isCalibrated: Boolean = false,
    val displayRotation: Int = 0,
    val isDrawerOpen: Boolean = false,
    val showSummaryDialog: Boolean = false,
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val currentAltitude: Double = 0.0,
    val sensorHealth: SensorHealth = SensorHealth(),
    val recoveredRide: RideSession? = null
)
