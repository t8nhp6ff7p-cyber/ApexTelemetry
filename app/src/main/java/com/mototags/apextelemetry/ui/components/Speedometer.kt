package com.mototags.apextelemetry.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.model.SpeedUnit
import com.mototags.apextelemetry.ui.theme.NeonOrange
import com.mototags.apextelemetry.ui.theme.SubtleGray

@Composable
fun Speedometer(
    speedKmh: Float,
    maxSpeedKmh: Float,
    avgSpeedKmh: Float,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    val displaySpeed = (speedKmh * speedUnit.factorToKmh).toInt()
    val displayMax = (maxSpeedKmh * speedUnit.factorToKmh).toInt()
    val displayAvg = (avgSpeedKmh * speedUnit.factorToKmh).toInt()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "GPS SPEED",
            style = MaterialTheme.typography.labelSmall,
            color = SubtleGray
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$displaySpeed",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
            color = Color.White,
            fontWeight = FontWeight.Black
        )

        Text(
            text = speedUnit.label,
            style = MaterialTheme.typography.labelSmall,
            color = SubtleGray,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Trip Speed Metrics: AVG & TOP
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "AVG", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SubtleGray)
                Text(
                    text = "$displayAvg ${speedUnit.label.lowercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "TOP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SubtleGray)
                Text(
                    text = "$displayMax ${speedUnit.label.lowercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = NeonOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
