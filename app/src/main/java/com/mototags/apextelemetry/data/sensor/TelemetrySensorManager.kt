package com.mototags.apextelemetry.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class TelemetrySensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val isGyroAvailable: Boolean = gyroSensor != null
    val isRotationVectorAvailable: Boolean = rotationVectorSensor != null

    var displayRotation: Int = Surface.ROTATION_0
    var invertDirection: Boolean = false

    var onLeanUpdated: ((leanAngle: Float) -> Unit)? = null
    var onGForceUpdated: ((lateralG: Float, longitudinalG: Float) -> Unit)? = null
    var onSensorHealthUpdated: ((rateHz: Int, vibrationIndex: Float) -> Unit)? = null

    // Matrix buffers
    private val rawRotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    
    // Baseline mount attitude matrix (Identity by default)
    private val tareInverseMatrix = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    )
    private val relativeMatrix = FloatArray(9)

    var isCalibrated: Boolean = false
        private set

    // Real-time sensor rate monitoring
    private var sampleCount = 0
    private var lastRateCalcTime = System.currentTimeMillis()
    private var currentSensorRateHz = 0

    // Mount vibration monitoring (detects loose phone mount)
    private var accelVarianceSum = 0f
    private var accelSampleCount = 0

    // Adaptive smoothing
    private var smoothedLeanAngle: Float = 0f
    private var smoothedLateralG: Float = 0f
    private var smoothedLongitudinalG: Float = 0f

    private val GRAVITY_MS2 = 9.80665f

    fun start() {
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        linearAccelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        lastRateCalcTime = System.currentTimeMillis()
        sampleCount = 0
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    /**
     * MANUAL CALIBRATION:
     * User holds motorcycle upright and taps "Tare Horizon / Calibrate".
     * Stores the inverse (transpose) of the current mount rotation matrix.
     */
    fun calibrateZero() {
        synchronized(this) {
            for (i in 0..2) {
                for (j in 0..2) {
                    tareInverseMatrix[i * 3 + j] = remappedMatrix[j * 3 + i]
                }
            }
            smoothedLeanAngle = 0f
            isCalibrated = true
        }
    }

    fun resetCalibration() {
        synchronized(this) {
            tareInverseMatrix[0] = 1f; tareInverseMatrix[1] = 0f; tareInverseMatrix[2] = 0f
            tareInverseMatrix[3] = 0f; tareInverseMatrix[4] = 1f; tareInverseMatrix[5] = 0f
            tareInverseMatrix[6] = 0f; tareInverseMatrix[7] = 0f; tareInverseMatrix[8] = 1f
            isCalibrated = false
            smoothedLeanAngle = 0f
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> handleRotationVector(event.values)
            Sensor.TYPE_LINEAR_ACCELERATION -> handleLinearAcceleration(event.values)
        }
    }

    private fun handleRotationVector(values: FloatArray) {
        // Monitor sensor update rate (Hz)
        sampleCount++
        val now = System.currentTimeMillis()
        if (now - lastRateCalcTime >= 1000L) {
            currentSensorRateHz = sampleCount
            sampleCount = 0
            val vibIndex = if (accelSampleCount > 0) (accelVarianceSum / accelSampleCount).coerceIn(0f, 1f) else 0f
            accelVarianceSum = 0f
            accelSampleCount = 0
            lastRateCalcTime = now
            onSensorHealthUpdated?.invoke(currentSensorRateHz, vibIndex)
        }

        SensorManager.getRotationMatrixFromVector(rawRotationMatrix, values)

        // Remap hardware axes according to screen rotation
        val (axisX, axisY) = when (displayRotation) {
            Surface.ROTATION_90 -> Pair(SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
            Surface.ROTATION_180 -> Pair(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
            Surface.ROTATION_270 -> Pair(SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
            else -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Y)
        }

        if (SensorManager.remapCoordinateSystem(rawRotationMatrix, axisX, axisY, remappedMatrix)) {
            synchronized(this) {
                val targetRollDeg: Float = if (isCalibrated) {
                    multiply3x3(tareInverseMatrix, remappedMatrix, relativeMatrix)

                    val gx = relativeMatrix[6]
                    val gy = relativeMatrix[7]
                    val gz = relativeMatrix[8]

                    val rollRad = atan2(gx.toDouble(), sqrt((gy * gy + gz * gz).toDouble()))
                    var deg = Math.toDegrees(rollRad).toFloat()
                    if (invertDirection) -deg else deg
                } else {
                    val gx = remappedMatrix[6]
                    val gy = remappedMatrix[7]
                    val gz = remappedMatrix[8]
                    val rollRad = atan2(gx.toDouble(), sqrt((gy * gy + gz * gz).toDouble()))
                    var deg = Math.toDegrees(rollRad).toFloat()
                    if (invertDirection) -deg else deg
                }

                val delta = abs(targetRollDeg - smoothedLeanAngle)
                val alpha = if (delta > 8f) 0.35f else 0.18f
                smoothedLeanAngle = (alpha * targetRollDeg) + ((1f - alpha) * smoothedLeanAngle)
            }

            onLeanUpdated?.invoke(smoothedLeanAngle)
        }
    }

    private fun handleLinearAcceleration(values: FloatArray) {
        val (screenX, screenY) = when (displayRotation) {
            Surface.ROTATION_90 -> Pair(values[1], -values[0])
            Surface.ROTATION_180 -> Pair(-values[0], -values[1])
            Surface.ROTATION_270 -> Pair(-values[1], values[0])
            else -> Pair(values[0], values[1])
        }

        val rawLatG = screenX / GRAVITY_MS2
        val rawLongG = screenY / GRAVITY_MS2

        // Mount vibration tracking (magnitude variance)
        val mag = sqrt((screenX * screenX + screenY * screenY).toDouble()).toFloat()
        accelVarianceSum += abs(mag - 0.5f) / 10f
        accelSampleCount++

        val alphaAccel = 0.20f
        smoothedLateralG = (alphaAccel * rawLatG) + ((1f - alphaAccel) * smoothedLateralG)
        smoothedLongitudinalG = (alphaAccel * rawLongG) + ((1f - alphaAccel) * smoothedLongitudinalG)

        onGForceUpdated?.invoke(smoothedLateralG, smoothedLongitudinalG)
    }

    private fun multiply3x3(a: FloatArray, b: FloatArray, result: FloatArray) {
        for (i in 0..2) {
            for (j in 0..2) {
                var sum = 0f
                for (k in 0..2) {
                    sum += a[i * 3 + k] * b[k * 3 + j]
                }
                result[i * 3 + j] = sum
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
