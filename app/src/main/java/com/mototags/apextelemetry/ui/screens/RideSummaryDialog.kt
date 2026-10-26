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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.model.TelemetryData
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.SubtleGray
import java.util.Locale

@Composable
fun RideSummaryDialog(
    data: TelemetryData,
    onSave: (String) -> Unit,
    onDiscard: () -> Unit
) {
    var title by remember { mutableStateOf("Twisty Ride Session") }

    val minutes = data.rideDurationSeconds / 60
    val seconds = data.rideDurationSeconds % 60
    val durationText = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    AlertDialog(
        onDismissRequest = onDiscard,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "🏁 RIDE COMPLETED",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column {
                Text(
                    text = "Session Duration: $durationText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtleGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SummaryMetric(label = "MAX LEFT", value = "${data.maxLeftLean.toInt()}°", color = MaterialTheme.colorScheme.primary)
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(SubtleGray.copy(alpha = 0.3f)))
                    SummaryMetric(label = "MAX RIGHT", value = "${data.maxRightLean.toInt()}°", color = MaterialTheme.colorScheme.primary)
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(SubtleGray.copy(alpha = 0.3f)))
                    SummaryMetric(label = "TOP SPEED", value = "${data.maxSpeedKmh.toInt()} km/h", color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Ride Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = SubtleGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SAVE RIDE", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDiscard) {
                Text("DISCARD", color = SubtleGray)
            }
        }
    )
}

@Composable
private fun SummaryMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SubtleGray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Black)
    }
}
