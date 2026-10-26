package com.mototags.apextelemetry.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.model.RideSession
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.NeonGreen
import com.mototags.apextelemetry.ui.theme.SubtleGray
import java.util.Locale

@Composable
fun RecoveryDialog(
    recoveredRide: RideSession,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val minutes = recoveredRide.durationSeconds / 60
    val seconds = recoveredRide.durationSeconds % 60
    val durationText = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val distanceText = String.format(Locale.US, "%.1f km", recoveredRide.distanceKm)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "⚡ UNFINISHED RIDE RECOVERED",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column {
                Text(
                    text = "A previously interrupted ride session was safely restored from disk Write-Ahead Logging.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TIME", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                        Text(durationText, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(SubtleGray.copy(alpha = 0.3f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DISTANCE", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                        Text(distanceText, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(SubtleGray.copy(alpha = 0.3f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOP SPEED", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                        Text("${recoveredRide.maxSpeedKmh.toInt()} km/h", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SAVE RECOVERED RIDE", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("DISCARD", color = SubtleGray)
            }
        }
    )
}
