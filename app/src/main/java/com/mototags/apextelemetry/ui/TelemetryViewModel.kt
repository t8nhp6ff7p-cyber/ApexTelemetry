package com.mototags.apextelemetry.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mototags.apextelemetry.data.database.RideRepository
import com.mototags.apextelemetry.data.export.RideExporter
import com.mototags.apextelemetry.data.location.LocationService
import com.mototags.apextelemetry.data.sensor.TelemetrySensorManager
import com.mototags.apextelemetry.model.DisplayStyle
import com.mototags.apextelemetry.model.GpsQuality
import com.mototags.apextelemetry.model.LeanScale
import com.mototags.apextelemetry.model.RideSession
import com.mototags.apextelemetry.model.RideState
import com.mototags.apextelemetry.model.SensorHealth
import com.mototags.apextelemetry.model.SpeedUnit
import com.mototags.apextelemetry.model.TelemetryData
import com.mototags.apextelemetry.model.TelemetryPoint
import com.mototags.apextelemetry.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

class TelemetryViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = TelemetrySensorManager(application)
    private val locationService = LocationService(application)
    private val rideRepository = RideRepository(application)
    private val rideExporter = RideExporter(application)
    private val intelligenceAnalyzer = com.mototags.apextelemetry.data.intelligence.RideIntelligenceAnalyzer()

    private val _telemetryState = MutableStateFlow(TelemetryData())
    val telemetryState: StateFlow<TelemetryData> = _telemetryState.asStateFlow()

    private val _savedRides = MutableStateFlow<List<RideSession>>(emptyList())
    val savedRides: StateFlow<List<RideSession>> = _savedRides.asStateFlow()

    private var rideTimerJob: Job? = null
    private var speedSumKmh = 0.0
    private var speedCount = 0L
    private var rideStartTime = 0L

    // In-memory telemetry log buffer for active ride
    private val recordedPoints = mutableListOf<TelemetryPoint>()
    private var lastWalSaveTime = 0L

    init {
        setupListeners()
        loadSavedRides()
        checkCrashRecovery()
    }

    private fun checkCrashRecovery() {
        val unfinished = rideRepository.getUnfinishedRideWal()
        if (unfinished != null && unfinished.durationSeconds > 10L) {
            _telemetryState.update { it.copy(recoveredRide = unfinished) }
        }
    }

    fun dismissRecovery() {
        rideRepository.clearActiveRideWal()
        _telemetryState.update { it.copy(recoveredRide = null) }
    }

    fun saveRecoveredRide() {
        val recovered = _telemetryState.value.recoveredRide ?: return
        rideRepository.saveRide(recovered)
        loadSavedRides()
        _telemetryState.update { it.copy(recoveredRide = null) }
    }

    private fun setupListeners() {
        sensorManager.onLeanUpdated = { leanAngle ->
            _telemetryState.update { current ->
                val limit = current.leanScale.maxDegrees.toFloat()
                val clamped = leanAngle.coerceIn(-limit, limit)

                if (current.rideState == RideState.RIDING) {
                    val left = if (clamped < 0f) abs(clamped) else 0f
                    val right = if (clamped > 0f) clamped else 0f

                    current.copy(
                        currentLeanAngle = clamped,
                        maxLeftLean = max(current.maxLeftLean, left),
                        maxRightLean = max(current.maxRightLean, right),
                        isCalibrated = sensorManager.isCalibrated
                    )
                } else {
                    current.copy(
                        currentLeanAngle = clamped,
                        isCalibrated = sensorManager.isCalibrated
                    )
                }
            }
        }

        sensorManager.onGForceUpdated = { latG, longG ->
            _telemetryState.update { current ->
                if (current.rideState == RideState.RIDING) {
                    val accel = if (longG > 0f) longG else 0f
                    val brake = if (longG < 0f) abs(longG) else 0f

                    current.copy(
                        lateralG = latG,
                        longitudinalG = longG,
                        maxLateralG = max(current.maxLateralG, abs(latG)),
                        maxAccelG = max(current.maxAccelG, accel),
                        maxBrakeG = max(current.maxBrakeG, brake)
                    )
                } else {
                    current.copy(lateralG = latG, longitudinalG = longG)
                }
            }
        }

        sensorManager.onSensorHealthUpdated = { hz, vibrationIndex ->
            _telemetryState.update { current ->
                current.copy(
                    sensorHealth = current.sensorHealth.copy(
                        sensorRateHz = hz,
                        mountVibrationLevel = vibrationIndex
                    )
                )
            }
        }

        locationService.onTelemetryLocation = { lat, lon, alt, speedKmh, accuracy, deltaDistKm, quality ->
            _telemetryState.update { current ->
                val newDist = current.tripDistanceKm + (if (current.rideState == RideState.RIDING) deltaDistKm else 0f)
                val newMax = if (current.rideState == RideState.RIDING) max(current.maxSpeedKmh, speedKmh) else current.maxSpeedKmh

                if (current.rideState == RideState.RIDING && speedKmh > 3.0f) {
                    speedSumKmh += speedKmh
                    speedCount++
                }
                val newAvg = if (speedCount > 0) (speedSumKmh / speedCount).toFloat() else 0f

                // Record telemetry point if active
                if (current.rideState == RideState.RIDING) {
                    val pt = TelemetryPoint(
                        timestamp = System.currentTimeMillis(),
                        speedKmh = speedKmh,
                        leanAngle = current.currentLeanAngle,
                        lateralG = current.lateralG,
                        longitudinalG = current.longitudinalG,
                        latitude = lat,
                        longitude = lon,
                        altitude = alt
                    )
                    synchronized(recordedPoints) {
                        recordedPoints.add(pt)
                    }

                    // Periodic Write-Ahead Logging (WAL) every 5 seconds
                    val now = System.currentTimeMillis()
                    if (now - lastWalSaveTime > 5000L) {
                        lastWalSaveTime = now
                        val currentSession = RideSession(
                            id = rideStartTime.toString(),
                            title = "In-Progress Ride",
                            startTime = rideStartTime,
                            endTime = now,
                            durationSeconds = current.rideDurationSeconds,
                            movingDurationSeconds = current.movingTimeSeconds,
                            distanceKm = newDist,
                            maxLeftLean = current.maxLeftLean,
                            maxRightLean = current.maxRightLean,
                            maxSpeedKmh = newMax,
                            avgSpeedKmh = newAvg,
                            maxLateralG = current.maxLateralG,
                            maxBrakeG = current.maxBrakeG,
                            points = ArrayList(recordedPoints)
                        )
                        rideRepository.saveActiveRideWal(currentSession)
                    }
                }

                current.copy(
                    currentLatitude = lat,
                    currentLongitude = lon,
                    currentAltitude = alt,
                    speedKmh = speedKmh,
                    maxSpeedKmh = newMax,
                    avgSpeedKmh = newAvg,
                    tripDistanceKm = newDist,
                    sensorHealth = current.sensorHealth.copy(
                        gpsAccuracyMeters = accuracy,
                        gpsQuality = quality,
                        totalDistanceKm = newDist
                    )
                )
            }
        }
    }

    fun startRide() {
        speedSumKmh = 0.0
        speedCount = 0L
        rideStartTime = System.currentTimeMillis()
        lastWalSaveTime = rideStartTime
        locationService.resetDistanceTracking()
        synchronized(recordedPoints) {
            recordedPoints.clear()
        }

        _telemetryState.update { current ->
            current.copy(
                rideState = RideState.RIDING,
                maxLeftLean = 0f,
                maxRightLean = 0f,
                maxLateralG = 0f,
                maxAccelG = 0f,
                maxBrakeG = 0f,
                maxSpeedKmh = 0f,
                avgSpeedKmh = 0f,
                tripDistanceKm = 0f,
                rideDurationSeconds = 0L,
                movingTimeSeconds = 0L,
                isDrawerOpen = false
            )
        }

        startTimer()
    }

    fun pauseRide() {
        rideTimerJob?.cancel()
        _telemetryState.update { it.copy(rideState = RideState.PAUSED) }
    }

    fun resumeRide() {
        _telemetryState.update { it.copy(rideState = RideState.RIDING) }
        startTimer()
    }

    fun finishRide() {
        rideTimerJob?.cancel()
        _telemetryState.update { it.copy(rideState = RideState.SUMMARY, showSummaryDialog = true) }
    }

    fun saveCurrentRide(title: String) {
        val state = _telemetryState.value
        val ptsCopy: List<TelemetryPoint>
        synchronized(recordedPoints) {
            ptsCopy = ArrayList(recordedPoints)
        }
        val intelligence = intelligenceAnalyzer.analyze(ptsCopy)

        val ride = RideSession(
            title = if (title.isBlank()) "Ride Session #${_savedRides.value.size + 1}" else title,
            startTime = rideStartTime,
            endTime = System.currentTimeMillis(),
            durationSeconds = state.rideDurationSeconds,
            movingDurationSeconds = state.movingTimeSeconds,
            distanceKm = state.tripDistanceKm,
            maxLeftLean = state.maxLeftLean,
            maxRightLean = state.maxRightLean,
            maxSpeedKmh = state.maxSpeedKmh,
            avgSpeedKmh = state.avgSpeedKmh,
            maxLateralG = state.maxLateralG,
            maxBrakeG = state.maxBrakeG,
            intelligence = intelligence,
            points = ptsCopy
        )

        rideRepository.saveRide(ride)
        loadSavedRides()
        _telemetryState.update { it.copy(rideState = RideState.READY, showSummaryDialog = false) }
    }

    fun discardRide() {
        rideRepository.clearActiveRideWal()
        _telemetryState.update { it.copy(rideState = RideState.READY, showSummaryDialog = false) }
    }

    fun deleteRide(id: String) {
        rideRepository.deleteRide(id)
        loadSavedRides()
    }

    fun exportRideGpx(ride: RideSession) {
        val file = rideExporter.exportToGpx(ride)
        rideExporter.shareFile(file, "application/gpx+xml")
    }

    fun exportRideKml(ride: RideSession) {
        val file = rideExporter.exportToKml(ride)
        rideExporter.shareFile(file, "application/vnd.google-earth.kml+xml")
    }

    fun exportRideCsv(ride: RideSession) {
        val file = rideExporter.exportToCsv(ride)
        rideExporter.shareFile(file, "text/csv")
    }

    fun calibrateManual() {
        sensorManager.calibrateZero()
        _telemetryState.update { it.copy(isCalibrated = true) }
    }

    fun resetCalibration() {
        sensorManager.resetCalibration()
        _telemetryState.update { it.copy(isCalibrated = false) }
    }

    fun toggleDirectionInvert() {
        val newInvert = !sensorManager.invertDirection
        sensorManager.invertDirection = newInvert
        _telemetryState.update { it.copy(isInvertedDirection = newInvert) }
    }

    fun setLeanScale(scale: LeanScale) {
        _telemetryState.update { it.copy(leanScale = scale) }
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        _telemetryState.update { it.copy(speedUnit = unit) }
    }

    fun setDisplayStyle(style: DisplayStyle) {
        _telemetryState.update { it.copy(displayStyle = style) }
    }

    fun setTheme(theme: AppTheme) {
        _telemetryState.update { it.copy(selectedTheme = theme) }
    }

    fun setDrawerOpen(open: Boolean) {
        _telemetryState.update { it.copy(isDrawerOpen = open) }
    }

    fun setDisplayRotation(rotation: Int) {
        sensorManager.displayRotation = rotation
        _telemetryState.update { it.copy(displayRotation = rotation) }
    }

    private fun startTimer() {
        rideTimerJob?.cancel()
        rideTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _telemetryState.update { current ->
                    val movingInc = if (current.speedKmh > 3.0f) 1L else 0L
                    current.copy(
                        rideDurationSeconds = current.rideDurationSeconds + 1,
                        movingTimeSeconds = current.movingTimeSeconds + movingInc
                    )
                }
            }
        }
    }

    fun loadSavedRides() {
        _savedRides.value = rideRepository.getAllRides()
    }

    fun startTelemetry() {
        sensorManager.start()
    }

    fun stopTelemetry() {
        sensorManager.stop()
    }

    fun startLocation() {
        locationService.startLocationUpdates()
    }

    fun stopLocation() {
        locationService.stopLocationUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        rideTimerJob?.cancel()
        sensorManager.stop()
        locationService.stopLocationUpdates()
    }
}
