package com.mototags.apextelemetry.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.model.GpsQuality
import com.mototags.apextelemetry.model.SensorHealth
import com.mototags.apextelemetry.model.SpeedUnit
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.NeonGreen
import com.mototags.apextelemetry.ui.theme.NeonOrange
import com.mototags.apextelemetry.ui.theme.NeonRed
import com.mototags.apextelemetry.ui.theme.SubtleGray
import java.util.Locale

@Composable
fun SensorHealthBar(
    health: SensorHealth,
    tripDistanceKm: Float,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    val displayDist = tripDistanceKm * speedUnit.factorToKmh
    val distUnitLabel = if (speedUnit == SpeedUnit.KMH) "KM" else "MI"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // GPS Quality & Accuracy
        val gpsColor = Color(health.gpsQuality.colorHex)
        val accText = if (health.gpsAccuracyMeters > 0f) "±${health.gpsAccuracyMeters.toInt()}m" else "--"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(gpsColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${health.gpsQuality.label} ($accText)",
                    color = gpsColor,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sensor Update Rate
        val rateText = if (health.sensorRateHz > 0) "${health.sensorRateHz}Hz" else "STANDBY"
        val rateColor = if (health.sensorRateHz >= 40) NeonGreen else SubtleGray
        Text(
            text = "IMU: $rateText",
            color = rateColor,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold
        )

        // Trip Odometer
        Text(
            text = String.format(Locale.US, "ODO: %.1f %s", displayDist, distUnitLabel),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Black
        )
    }
}
