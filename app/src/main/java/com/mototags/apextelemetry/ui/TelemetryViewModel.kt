package com.mototags.apextelemetry.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mototags.apextelemetry.data.location.LocationService
import com.mototags.apextelemetry.data.sensor.TelemetrySensorManager
import com.mototags.apextelemetry.model.TelemetryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.max

class TelemetryViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = TelemetrySensorManager(application)
    private val locationService = LocationService(application)

    private val _telemetryState = MutableStateFlow(TelemetryData())
    val telemetryState: StateFlow<TelemetryData> = _telemetryState.asStateFlow()

    init {
        setupListeners()
    }

    private fun setupListeners() {
        sensorManager.onLeanUpdated = { leanAngle ->
            _telemetryState.update { current ->
                val leftLean = if (leanAngle < 0f) abs(leanAngle) else 0f
                val rightLean = if (leanAngle > 0f) leanAngle else 0f

                current.copy(
                    currentLeanAngle = leanAngle,
                    maxLeftLean = max(current.maxLeftLean, leftLean),
                    maxRightLean = max(current.maxRightLean, rightLean)
                )
            }
        }

        sensorManager.onGForceUpdated = { latG, longG ->
            _telemetryState.update { current ->
                val accel = if (longG > 0f) longG else 0f
                val brake = if (longG < 0f) abs(longG) else 0f

                current.copy(
                    lateralG = latG,
                    longitudinalG = longG,
                    maxLateralG = max(current.maxLateralG, abs(latG)),
                    maxAccelG = max(current.maxAccelG, accel),
                    maxBrakeG = max(current.maxBrakeG, brake)
                )
            }
        }

        locationService.onSpeedUpdated = { speedKmh ->
            _telemetryState.update { current ->
                current.copy(
                    speedKmh = speedKmh,
                    maxSpeedKmh = max(current.maxSpeedKmh, speedKmh)
                )
            }
        }
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

    fun setDisplayRotation(rotation: Int) {
        sensorManager.displayRotation = rotation
        _telemetryState.update { it.copy(displayRotation = rotation) }
    }

    fun calibrateZero() {
        sensorManager.calibrateZero()
        _telemetryState.update { it.copy(isCalibrated = true) }
    }

    fun resetPeaks() {
        _telemetryState.update { current ->
            current.copy(
                maxLeftLean = 0f,
                maxRightLean = 0f,
                maxLateralG = 0f,
                maxAccelG = 0f,
                maxBrakeG = 0f,
                maxSpeedKmh = 0f
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stop()
        locationService.stopLocationUpdates()
    }
}
