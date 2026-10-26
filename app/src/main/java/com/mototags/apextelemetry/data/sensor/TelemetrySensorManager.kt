package com.mototags.apextelemetry.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import kotlin.math.atan2
import kotlin.math.sqrt

class TelemetrySensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    // Current display rotation from window manager (Surface.ROTATION_0, 90, 180, 270)
    var displayRotation: Int = Surface.ROTATION_0

    // Callback listeners
    var onLeanUpdated: ((leanAngle: Float) -> Unit)? = null
    var onGForceUpdated: ((lateralG: Float, longitudinalG: Float) -> Unit)? = null

    // Matrices for orientation calculation
    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Calibration zero-offsets
    private var zeroRollOffset: Float = 0f
    private var isCalibrated: Boolean = false

    // Exponential Moving Average (EMA) smoothing to eliminate engine & road vibration
    // alpha = 0.15 gives responsiveness while eliminating high-frequency handlebar buzz
    private val alphaLean = 0.20f
    private val alphaAccel = 0.15f

    private var smoothedLeanAngle: Float = 0f
    private var smoothedLateralG: Float = 0f
    private var smoothedLongitudinalG: Float = 0f

    private val GRAVITY_MS2 = 9.80665f

    fun start() {
        // SensorManager.SENSOR_DELAY_GAME (~20ms / 50Hz) or SENSOR_DELAY_FASTEST
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        linearAccelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Tares current bike lean angle as 0° (upright bike position)
     */
    fun calibrateZero() {
        zeroRollOffset = smoothedLeanAngle + zeroRollOffset
        isCalibrated = true
    }

    fun resetCalibration() {
        zeroRollOffset = 0f
        isCalibrated = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> handleRotationVector(event.values)
            Sensor.TYPE_LINEAR_ACCELERATION -> handleLinearAcceleration(event.values)
        }
    }

    private fun handleRotationVector(values: FloatArray) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)

        // Dynamically remap sensor coordinate system according to screen orientation
        val (axisX, axisY) = when (displayRotation) {
            Surface.ROTATION_90 -> Pair(SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
            Surface.ROTATION_180 -> Pair(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
            Surface.ROTATION_270 -> Pair(SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
            else -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Y) // Surface.ROTATION_0
        }

        if (SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remappedMatrix)) {
            SensorManager.getOrientation(remappedMatrix, orientationAngles)
            // Roll corresponds to orientationAngles[2] in radians (-PI to PI)
            val rawRollRadians = orientationAngles[2]
            var rawRollDegrees = Math.toDegrees(rawRollRadians.toDouble()).toFloat()

            // Invert sign so leaning right is positive (+), leaning left is negative (-)
            val calibratedDegrees = rawRollDegrees - zeroRollOffset

            // Filter out vibration
            smoothedLeanAngle = (alphaLean * calibratedDegrees) + ((1f - alphaLean) * smoothedLeanAngle)
            onLeanUpdated?.invoke(smoothedLeanAngle)
        }
    }

    private fun handleLinearAcceleration(values: FloatArray) {
        // Remap raw acceleration axes based on screen orientation
        val (screenX, screenY) = when (displayRotation) {
            Surface.ROTATION_90 -> Pair(values[1], -values[0])
            Surface.ROTATION_180 -> Pair(-values[0], -values[1])
            Surface.ROTATION_270 -> Pair(-values[1], values[0])
            else -> Pair(values[0], values[1])
        }

        // Convert to G-units (1G = 9.80665 m/s^2)
        val rawLatG = screenX / GRAVITY_MS2
        val rawLongG = screenY / GRAVITY_MS2

        // EMA smoothing
        smoothedLateralG = (alphaAccel * rawLatG) + ((1f - alphaAccel) * smoothedLateralG)
        smoothedLongitudinalG = (alphaAccel * rawLongG) + ((1f - alphaAccel) * smoothedLongitudinalG)

        onGForceUpdated?.invoke(smoothedLateralG, smoothedLongitudinalG)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action required
    }
}
