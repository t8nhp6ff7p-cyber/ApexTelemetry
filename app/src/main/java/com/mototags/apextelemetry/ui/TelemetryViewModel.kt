package com.mototags.apextelemetry.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mototags.apextelemetry.data.database.RideRepository
import com.mototags.apextelemetry.data.location.LocationService
import com.mototags.apextelemetry.data.sensor.TelemetrySensorManager
import com.mototags.apextelemetry.model.RideSession
import com.mototags.apextelemetry.model.RideState
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
    private var speedSamples = mutableListOf<Float>()
    private var rideStartTime: Long = 0L

    init {
        setupListeners()
        loadSavedRides()
    }

    private fun setupListeners() {
        sensorManager.onLeanUpdated = { leanAngle ->
            _telemetryState.update { current ->
                if (current.rideState == RideState.RIDING) {
                    val leftLean = if (leanAngle < 0f) abs(leanAngle) else 0f
                    val rightLean = if (leanAngle > 0f) leanAngle else 0f

                    current.copy(
                        currentLeanAngle = leanAngle,
                        maxLeftLean = max(current.maxLeftLean, leftLean),
                        maxRightLean = max(current.maxRightLean, rightLean),
                        isCalibrated = sensorManager.isCalibrated
                    )
                } else {
                    // Preview lean angle even when not recording
                    current.copy(
                        currentLeanAngle = leanAngle,
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
                    speedSamples.add(speedKmh)
                    val avg = if (speedSamples.isNotEmpty()) speedSamples.average().toFloat() else 0f
                    current.copy(
                        speedKmh = speedKmh,
                        maxSpeedKmh = max(current.maxSpeedKmh, speedKmh),
                        avgSpeedKmh = avg
                    )
                } else {
                    current.copy(speedKmh = speedKmh)
                }
            }
        }
    }

    fun startRide() {
        // Automatically tare/zero orientation at ride start
        sensorManager.calibrateZero()
        speedSamples.clear()
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
                isCalibrated = true
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
        _telemetryState.update { it.copy(rideState = RideState.SUMMARY) }
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
        _telemetryState.update { it.copy(rideState = RideState.READY, activeTab = 1) }
    }

    fun discardRide() {
        _telemetryState.update { it.copy(rideState = RideState.READY) }
    }

    fun deleteRide(id: String) {
        rideRepository.deleteRide(id)
        loadSavedRides()
    }

    private fun startTimer() {
        rideTimerJob?.cancel()
        rideTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _telemetryState.update { it.copy(rideDurationSeconds = it.rideDurationSeconds + 1) }
            }
        }
    }

    fun loadSavedRides() {
        _savedRides.value = rideRepository.getAllRides()
    }

    fun setTheme(theme: AppTheme) {
        _telemetryState.update { it.copy(selectedTheme = theme) }
    }

    fun setDisplayStyle(style: com.mototags.apextelemetry.model.DisplayStyle) {
        _telemetryState.update { it.copy(displayStyle = style) }
    }

    fun toggleDirectionInvert() {
        val newInvert = !sensorManager.invertDirection
        sensorManager.invertDirection = newInvert
        _telemetryState.update { it.copy(isInvertedDirection = newInvert) }
    }

    fun setActiveTab(tabIndex: Int) {
        _telemetryState.update { it.copy(activeTab = tabIndex) }
    }

    fun calibrateZero() {
        sensorManager.calibrateZero()
        _telemetryState.update { it.copy(isCalibrated = true) }
    }

    fun setDisplayRotation(rotation: Int) {
        sensorManager.displayRotation = rotation
        _telemetryState.update { it.copy(displayRotation = rotation) }
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
