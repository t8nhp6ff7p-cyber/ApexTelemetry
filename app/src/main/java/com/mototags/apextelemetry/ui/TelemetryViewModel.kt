package com.mototags.apextelemetry.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mototags.apextelemetry.data.database.RideRepository
import com.mototags.apextelemetry.data.location.LocationService
import com.mototags.apextelemetry.data.sensor.TelemetrySensorManager
import com.mototags.apextelemetry.model.DisplayStyle
import com.mototags.apextelemetry.model.LeanScale
import com.mototags.apextelemetry.model.RideSession
import com.mototags.apextelemetry.model.RideState
import com.mototags.apextelemetry.model.SpeedUnit
import com.mototags.apextelemetry.model.TelemetryData
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

    private val _telemetryState = MutableStateFlow(TelemetryData())
    val telemetryState: StateFlow<TelemetryData> = _telemetryState.asStateFlow()

    private val _savedRides = MutableStateFlow<List<RideSession>>(emptyList())
    val savedRides: StateFlow<List<RideSession>> = _savedRides.asStateFlow()

    private var rideTimerJob: Job? = null
    private var speedSumKmh = 0.0
    private var speedCount = 0L
    private var rideStartTime = 0L

    init {
        setupListeners()
        loadSavedRides()
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

        locationService.onSpeedUpdated = { speedKmh ->
            _telemetryState.update { current ->
                if (current.rideState == RideState.RIDING) {
                    val newMax = max(current.maxSpeedKmh, speedKmh)
                    if (speedKmh > 3.0f) {
                        speedSumKmh += speedKmh
                        speedCount++
                    }
                    val newAvg = if (speedCount > 0) (speedSumKmh / speedCount).toFloat() else 0f

                    current.copy(
                        speedKmh = speedKmh,
                        maxSpeedKmh = newMax,
                        avgSpeedKmh = newAvg
                    )
                } else {
                    current.copy(speedKmh = speedKmh)
                }
            }
        }
    }

    /**
     * Start Ride WITHOUT forced auto-calibration.
     * Rider has full manual control over when to zero/calibrate.
     */
    fun startRide() {
        speedSumKmh = 0.0
        speedCount = 0L
        rideStartTime = System.currentTimeMillis()

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
        val ride = RideSession(
            title = if (title.isBlank()) "Ride Session #${_savedRides.value.size + 1}" else title,
            startTime = rideStartTime,
            endTime = System.currentTimeMillis(),
            durationSeconds = state.rideDurationSeconds,
            maxLeftLean = state.maxLeftLean,
            maxRightLean = state.maxRightLean,
            maxSpeedKmh = state.maxSpeedKmh,
            avgSpeedKmh = state.avgSpeedKmh,
            maxLateralG = state.maxLateralG,
            maxBrakeG = state.maxBrakeG
        )

        rideRepository.saveRide(ride)
        loadSavedRides()
        _telemetryState.update { it.copy(rideState = RideState.READY, showSummaryDialog = false) }
    }

    fun discardRide() {
        _telemetryState.update { it.copy(rideState = RideState.READY, showSummaryDialog = false) }
    }

    fun deleteRide(id: String) {
        rideRepository.deleteRide(id)
        loadSavedRides()
    }

    /**
     * MANUAL ZERO CALIBRATION
     */
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
