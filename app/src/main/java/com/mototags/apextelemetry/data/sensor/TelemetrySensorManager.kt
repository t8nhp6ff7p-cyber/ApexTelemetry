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
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    var displayRotation: Int = Surface.ROTATION_0

    var onLeanUpdated: ((leanAngle: Float) -> Unit)? = null
    var onGForceUpdated: ((lateralG: Float, longitudinalG: Float) -> Unit)? = null

    // Matrix buffers
    private val rawRotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    
    // Mount calibration baseline matrix (Identity by default)
    private val tareInverseMatrix = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    )
    private val relativeMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    var isCalibrated: Boolean = false
        private set

    // Vibration low-pass filter
    private val alphaLean = 0.22f
    private val alphaAccel = 0.18f

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
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Tares current bike mount orientation as the true upright 0° reference.
     * Computes the transpose (inverse) of the current rotation matrix.
     */
    fun calibrateZero() {
        synchronized(this) {
            // Transpose remappedMatrix into tareInverseMatrix
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
        SensorManager.getRotationMatrixFromVector(rawRotationMatrix, values)

        // Remap hardware axes according to current screen orientation
        val (axisX, axisY) = when (displayRotation) {
            Surface.ROTATION_90 -> Pair(SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
            Surface.ROTATION_180 -> Pair(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
            Surface.ROTATION_270 -> Pair(SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
            else -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Y)
        }

        if (SensorManager.remapCoordinateSystem(rawRotationMatrix, axisX, axisY, remappedMatrix)) {
            synchronized(this) {
                if (isCalibrated) {
                    // Multiply: relativeMatrix = tareInverseMatrix * remappedMatrix
                    multiply3x3(tareInverseMatrix, remappedMatrix, relativeMatrix)

                    // Extract pure roll angle from relative matrix
                    // In the calibrated reference frame, roll is rotation around the forward Y axis
                    val rollRad = atan2(relativeMatrix[6].toDouble(), relativeMatrix[7].toDouble())
                    var rollDeg = Math.toDegrees(rollRad).toFloat()

                    // Normalize to -180..180
                    if (rollDeg > 180f) rollDeg -= 360f
                    if (rollDeg < -180f) rollDeg += 360f

                    // Leaning right is positive (+), leaning left is negative (-)
                    val targetAngle = rollDeg.coerceIn(-75f, 75f)
                    smoothedLeanAngle = (alphaLean * targetAngle) + ((1f - alphaLean) * smoothedLeanAngle)
                } else {
                    // When untared, compute basic level roll for calibration preview
                    SensorManager.getOrientation(remappedMatrix, orientationAngles)
                    val rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
                    smoothedLeanAngle = (alphaLean * rawRoll) + ((1f - alphaLean) * smoothedLeanAngle)
                }
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
