package com.mototags.apextelemetry.data.database

import android.content.Context
import android.content.SharedPreferences
import com.mototags.apextelemetry.model.RideSession
import org.json.JSONArray
import org.json.JSONObject

class RideRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("apex_telemetry_rides", Context.MODE_PRIVATE)

    private val KEY_SAVED_RIDES = "saved_rides_json"

    fun getAllRides(): List<RideSession> {
        val jsonString = prefs.getString(KEY_SAVED_RIDES, null) ?: return emptyList()
        val list = mutableListOf<RideSession>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    RideSession(
                        id = obj.optString("id"),
                        title = obj.optString("title", "Ride Session"),
                        startTime = obj.optLong("startTime"),
                        endTime = obj.optLong("endTime"),
                        durationSeconds = obj.optLong("durationSeconds"),
                        maxLeftLean = obj.optDouble("maxLeftLean", 0.0).toFloat(),
                        maxRightLean = obj.optDouble("maxRightLean", 0.0).toFloat(),
                        maxSpeedKmh = obj.optDouble("maxSpeedKmh", 0.0).toFloat(),
                        avgSpeedKmh = obj.optDouble("avgSpeedKmh", 0.0).toFloat(),
                        maxLateralG = obj.optDouble("maxLateralG", 0.0).toFloat(),
                        maxBrakeG = obj.optDouble("maxBrakeG", 0.0).toFloat(),
                        totalCornersLogged = obj.optInt("totalCornersLogged", 0)
                    )
                )
            }
        } catch (_: Exception) {
            // Fallback on corrupt JSON
        }
        return list.sortedByDescending { it.startTime }
    }

    fun saveRide(ride: RideSession) {
        val currentList = getAllRides().toMutableList()
        currentList.add(0, ride)
        persistList(currentList)
    }

    fun deleteRide(id: String) {
        val filtered = getAllRides().filterNot { it.id == id }
        persistList(filtered)
    }

    private fun persistList(list: List<RideSession>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("startTime", item.startTime)
                put("endTime", item.endTime)
                put("durationSeconds", item.durationSeconds)
                put("maxLeftLean", item.maxLeftLean.toDouble())
                put("maxRightLean", item.maxRightLean.toDouble())
                put("maxSpeedKmh", item.maxSpeedKmh.toDouble())
                put("avgSpeedKmh", item.avgSpeedKmh.toDouble())
                put("maxLateralG", item.maxLateralG.toDouble())
                put("maxBrakeG", item.maxBrakeG.toDouble())
                put("totalCornersLogged", item.totalCornersLogged)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_SAVED_RIDES, jsonArray.toString()).apply()
    }
}
