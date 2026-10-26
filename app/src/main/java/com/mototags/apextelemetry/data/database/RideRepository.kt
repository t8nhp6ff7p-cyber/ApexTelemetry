package com.mototags.apextelemetry.data.database

import android.content.Context
import android.content.SharedPreferences
import com.mototags.apextelemetry.model.RideSession
import com.mototags.apextelemetry.model.TelemetryPoint
import org.json.JSONArray
import org.json.JSONObject

class RideRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("apex_telemetry_rides", Context.MODE_PRIVATE)

    private val KEY_SAVED_RIDES = "saved_rides_json"
    private val KEY_ACTIVE_WAL = "active_ride_wal_json"

    fun getAllRides(): List<RideSession> {
        val jsonString = prefs.getString(KEY_SAVED_RIDES, null) ?: return emptyList()
        val list = mutableListOf<RideSession>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(parseRide(obj))
            }
        } catch (_: Exception) {}
        return list.sortedByDescending { it.startTime }
    }

    fun saveRide(ride: RideSession) {
        val currentList = getAllRides().toMutableList()
        currentList.removeAll { it.id == ride.id }
        currentList.add(0, ride)
        persistList(currentList)
        clearActiveRideWal()
    }

    fun deleteRide(id: String) {
        val filtered = getAllRides().filterNot { it.id == id }
        persistList(filtered)
    }

    // Write-Ahead Logging (WAL) to ensure crashes / battery dead / phone restarts never lose rides
    fun saveActiveRideWal(ride: RideSession) {
        try {
            val obj = serializeRide(ride)
            prefs.edit().putString(KEY_ACTIVE_WAL, obj.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getUnfinishedRideWal(): RideSession? {
        val jsonString = prefs.getString(KEY_ACTIVE_WAL, null) ?: return null
        return try {
            val obj = JSONObject(jsonString)
            parseRide(obj).copy(isRecovered = true)
        } catch (_: Exception) {
            null
        }
    }

    fun clearActiveRideWal() {
        prefs.edit().remove(KEY_ACTIVE_WAL).apply()
    }

    private fun persistList(list: List<RideSession>) {
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(serializeRide(item))
        }
        prefs.edit().putString(KEY_SAVED_RIDES, jsonArray.toString()).apply()
    }

    private fun serializeRide(item: RideSession): JSONObject {
        val obj = JSONObject().apply {
            put("id", item.id)
            put("title", item.title)
            put("startTime", item.startTime)
            put("endTime", item.endTime)
            put("durationSeconds", item.durationSeconds)
            put("movingDurationSeconds", item.movingDurationSeconds)
            put("distanceKm", item.distanceKm.toDouble())
            put("maxLeftLean", item.maxLeftLean.toDouble())
            put("maxRightLean", item.maxRightLean.toDouble())
            put("maxSpeedKmh", item.maxSpeedKmh.toDouble())
            put("avgSpeedKmh", item.avgSpeedKmh.toDouble())
            put("maxLateralG", item.maxLateralG.toDouble())
            put("maxBrakeG", item.maxBrakeG.toDouble())

            val ptsArray = JSONArray()
            for (p in item.points) {
                val ptObj = JSONObject().apply {
                    put("t", p.timestamp)
                    put("s", p.speedKmh.toDouble())
                    put("l", p.leanAngle.toDouble())
                    put("lg", p.lateralG.toDouble())
                    put("bg", p.longitudinalG.toDouble())
                    put("lat", p.latitude)
                    put("lon", p.longitude)
                    put("alt", p.altitude)
                }
                ptsArray.put(ptObj)
            }
            put("points", ptsArray)
        }
        return obj
    }

    private fun parseRide(obj: JSONObject): RideSession {
        val ptsList = mutableListOf<TelemetryPoint>()
        val ptsArray = obj.optJSONArray("points")
        if (ptsArray != null) {
            for (j in 0 until ptsArray.length()) {
                val p = ptsArray.getJSONObject(j)
                ptsList.add(
                    TelemetryPoint(
                        timestamp = p.optLong("t"),
                        speedKmh = p.optDouble("s", 0.0).toFloat(),
                        leanAngle = p.optDouble("l", 0.0).toFloat(),
                        lateralG = p.optDouble("lg", 0.0).toFloat(),
                        longitudinalG = p.optDouble("bg", 0.0).toFloat(),
                        latitude = p.optDouble("lat", 0.0),
                        longitude = p.optDouble("lon", 0.0),
                        altitude = p.optDouble("alt", 0.0)
                    )
                )
            }
        }

        return RideSession(
            id = obj.optString("id"),
            title = obj.optString("title", "Ride Session"),
            startTime = obj.optLong("startTime"),
            endTime = obj.optLong("endTime"),
            durationSeconds = obj.optLong("durationSeconds"),
            movingDurationSeconds = obj.optLong("movingDurationSeconds"),
            distanceKm = obj.optDouble("distanceKm", 0.0).toFloat(),
            maxLeftLean = obj.optDouble("maxLeftLean", 0.0).toFloat(),
            maxRightLean = obj.optDouble("maxRightLean", 0.0).toFloat(),
            maxSpeedKmh = obj.optDouble("maxSpeedKmh", 0.0).toFloat(),
            avgSpeedKmh = obj.optDouble("avgSpeedKmh", 0.0).toFloat(),
            maxLateralG = obj.optDouble("maxLateralG", 0.0).toFloat(),
            maxBrakeG = obj.optDouble("maxBrakeG", 0.0).toFloat(),
            points = ptsList
        )
    }
}
