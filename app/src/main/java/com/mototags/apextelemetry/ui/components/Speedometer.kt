package com.mototags.apextelemetry.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.ui.theme.NeonOrange
import com.mototags.apextelemetry.ui.theme.SubtleGray

@Composable
fun Speedometer(
    speedKmh: Float,
    maxSpeedKmh: Float,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "GPS SPEED",
            style = MaterialTheme.typography.labelSmall,
            color = SubtleGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${speedKmh.toInt()}",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
            color = Color.White,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "KM / H",
            style = MaterialTheme.typography.labelSmall,
            color = SubtleGray,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "TOP: ${maxSpeedKmh.toInt()} km/h",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = NeonOrange,
            fontWeight = FontWeight.Bold
        )
    }
}
