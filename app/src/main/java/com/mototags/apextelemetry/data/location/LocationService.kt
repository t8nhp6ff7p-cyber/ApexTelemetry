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

class LocationService(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    var onSpeedUpdated: ((speedKmh: Float) -> Unit)? = null

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        500L // 500 ms interval for responsive speedometer
    ).apply {
        setMinUpdateIntervalMillis(250L)
        setMinUpdateDistanceMeters(0.5f)
    }.build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                // location.speed is in m/s. Convert to km/h (1 m/s = 3.6 km/h)
                val speedKmh = if (location.hasSpeed()) {
                    location.speed * 3.6f
                } else {
                    0f
                }
                onSpeedUpdated?.invoke(speedKmh.coerceAtLeast(0f))
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
            // Handled in UI layer via permission request
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
