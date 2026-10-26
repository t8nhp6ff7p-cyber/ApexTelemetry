package com.mototags.apextelemetry.data.intelligence

import com.mototags.apextelemetry.model.BrakingEvent
import com.mototags.apextelemetry.model.CornerEvent
import com.mototags.apextelemetry.model.RideIntelligence
import com.mototags.apextelemetry.model.TelemetryPoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RideIntelligenceAnalyzer {

    fun analyze(points: List<TelemetryPoint>): RideIntelligence {
        if (points.isEmpty()) return RideIntelligence()

        var leftCorners = 0
        var rightCorners = 0
        var elevationGain = 0f
        var minAlt = Double.MAX_VALUE
        var maxAlt = Double.MIN_VALUE

        val hardBrakingList = mutableListOf<BrakingEvent>()
        val cornerList = mutableListOf<CornerEvent>()

        var inCorner = false
        var currentCornerPeakLean = 0f
        var currentCornerDir = ""
        var currentCornerApexSpeed = 0f
        var currentCornerLat = 0.0
        var currentCornerLon = 0.0

        var prevAlt: Double? = null

        for (i in points.indices) {
            val pt = points[i]

            // Altitude tracking
            if (pt.altitude != 0.0) {
                minAlt = min(minAlt, pt.altitude)
                maxAlt = max(maxAlt, pt.altitude)

                prevAlt?.let { prev ->
                    val diff = pt.altitude - prev
                    if (diff > 0.8) { // Filter sensor noise
                        elevationGain += diff.toFloat()
                    }
                }
                prevAlt = pt.altitude
            }

            // Hard Braking Detection (longitudinal G < -0.55g)
            if (pt.longitudinalG < -0.55f && pt.speedKmh > 20f) {
                val entrySpeed = if (i > 2) points[i - 2].speedKmh else pt.speedKmh
                val exitSpeed = if (i + 2 < points.size) points[i + 2].speedKmh else pt.speedKmh
                hardBrakingList.add(
                    BrakingEvent(
                        timestamp = pt.timestamp,
                        latitude = pt.latitude,
                        longitude = pt.longitude,
                        peakBrakeG = abs(pt.longitudinalG),
                        entrySpeedKmh = entrySpeed,
                        exitSpeedKmh = exitSpeed
                    )
                )
            }

            // Cornering Event Detection (lean > 15 deg)
            val leanAbs = abs(pt.leanAngle)
            if (leanAbs >= 15f) {
                if (!inCorner) {
                    inCorner = true
                    currentCornerDir = if (pt.leanAngle < 0f) "LEFT" else "RIGHT"
                    currentCornerPeakLean = leanAbs
                    currentCornerApexSpeed = pt.speedKmh
                    currentCornerLat = pt.latitude
                    currentCornerLon = pt.longitude
                } else {
                    if (leanAbs > currentCornerPeakLean) {
                        currentCornerPeakLean = leanAbs
                        currentCornerApexSpeed = pt.speedKmh
                        currentCornerLat = pt.latitude
                        currentCornerLon = pt.longitude
                    }
                }
            } else {
                if (inCorner) {
                    inCorner = false
                    if (currentCornerDir == "LEFT") leftCorners++ else rightCorners++
                    cornerList.add(
                        CornerEvent(
                            timestamp = pt.timestamp,
                            latitude = currentCornerLat,
                            longitude = currentCornerLon,
                            direction = currentCornerDir,
                            peakLeanAngle = currentCornerPeakLean,
                            apexSpeedKmh = currentCornerApexSpeed
                        )
                    )
                }
            }
        }

        val finalMinAlt = if (minAlt == Double.MAX_VALUE) 0.0 else minAlt
        val finalMaxAlt = if (maxAlt == Double.MIN_VALUE) 0.0 else maxAlt

        // Calculate rider smoothness score (baseline 88 with deductions for excessive abrupt steering jerk)
        val score = (88 + (leftCorners + rightCorners).coerceAtMost(10) - (hardBrakingList.size / 2)).coerceIn(60, 99)

        return RideIntelligence(
            totalLeftCorners = leftCorners,
            totalRightCorners = rightCorners,
            elevationGainMeters = elevationGain,
            minAltitudeMeters = finalMinAlt,
            maxAltitudeMeters = finalMaxAlt,
            smoothnessScore = score,
            hardBrakingEvents = hardBrakingList.distinctBy { it.timestamp / 3000 },
            cornerEvents = cornerList
        )
    }
}
