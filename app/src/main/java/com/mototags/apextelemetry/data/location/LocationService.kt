package com.mototags.apextelemetry.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mototags.apextelemetry.model.GpsQuality

class LocationService(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    var onTelemetryLocation: ((
        lat: Double,
        lon: Double,
        alt: Double,
        speedKmh: Float,
        accuracyMeters: Float,
        deltaDistanceKm: Float,
        gpsQuality: GpsQuality
    ) -> Unit)? = null

    private var lastValidLocation: Location? = null

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        500L // 2Hz update rate for responsive speedometer & telemetry trace
    ).apply {
        setMinUpdateIntervalMillis(250L)
        setMinUpdateDistanceMeters(0.5f)
    }.build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                val accuracy = if (location.hasAccuracy()) location.accuracy else 99f

                val quality = when {
                    accuracy <= 5f -> GpsQuality.EXCELLENT
                    accuracy <= 12f -> GpsQuality.GOOD
                    accuracy <= 25f -> GpsQuality.FAIR
                    accuracy <= 50f -> GpsQuality.WEAK
                    else -> GpsQuality.NO_LOCK
                }

                // GPS Outlier Filtering: ignore crazy spikes (> 280 km/h) caused by multipath satellite bounce
                var speedKmh = if (location.hasSpeed()) location.speed * 3.6f else 0f
                if (speedKmh > 280f) {
                    speedKmh = lastValidLocation?.speed?.times(3.6f) ?: 0f
                }
                speedKmh = speedKmh.coerceAtLeast(0f)

                // High-precision distance integration
                var deltaDistanceKm = 0f
                val prev = lastValidLocation
                if (prev != null) {
                    val distMeters = location.distanceTo(prev)
                    // Only accumulate distance if accuracy is acceptable and speed indicates real motion
                    if (accuracy < 30f && (distMeters in 0.5f..500f)) {
                        deltaDistanceKm = distMeters / 1000f
                    }
                }

                lastValidLocation = location

                onTelemetryLocation?.invoke(
                    location.latitude,
                    location.longitude,
                    location.altitude,
                    speedKmh,
                    accuracy,
                    deltaDistanceKm,
                    quality
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            // Handled via permission request
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        lastValidLocation = null
    }

    fun resetDistanceTracking() {
        lastValidLocation = null
    }
}
