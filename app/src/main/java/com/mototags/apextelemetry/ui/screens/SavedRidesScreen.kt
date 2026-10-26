package com.mototags.apextelemetry.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.mototags.apextelemetry.ui.theme.PureBlack
import com.mototags.apextelemetry.ui.theme.SubtleGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedRidesScreen(
    rides: List<RideSession>,
    onSelectRide: (RideSession) -> Unit,
    onDeleteRide: (String) -> Unit,
    onExportGpx: (RideSession) -> Unit,
    onExportCsv: (RideSession) -> Unit,
    modifier: Modifier = Modifier
) {
    if (rides.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(PureBlack)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = SubtleGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "NO SAVED RIDES YET",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start a ride from the Cockpit HUD, ride your favorite twisties, and hit Save. Telemetry will be saved with GPX & CSV export ready!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtleGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(PureBlack)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "SAVED RIDE LOGS (${rides.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(rides, key = { it.id }) { ride ->
                RideCard(
                    ride = ride,
                    onClick = { onSelectRide(ride) },
                    onDelete = { onDeleteRide(ride.id) },
                    onExportGpx = { onExportGpx(ride) },
                    onExportCsv = { onExportCsv(ride) }
                )
            }
        }
    }
}

@Composable
private fun RideCard(
    ride: RideSession,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onExportGpx: () -> Unit,
    onExportCsv: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(ride.startTime))
    val minutes = ride.durationSeconds / 60
    val seconds = ride.durationSeconds % 60
    val durationStr = String.format(Locale.US, "%02d:%02d min", minutes, seconds)
    val distanceStr = String.format(Locale.US, "%.1f km", ride.distanceKm)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = ride.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "$dateStr • $durationStr • $distanceStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = SubtleGray
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = SubtleGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricColumn(
                    label = "MAX L / R",
                    value = "${ride.maxLeftLean.toInt()}° / ${ride.maxRightLean.toInt()}°",
                    highlightColor = MaterialTheme.colorScheme.primary
                )
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(SubtleGray.copy(alpha = 0.3f)))
                MetricColumn(
                    label = "AVG / TOP",
                    value = "${ride.avgSpeedKmh.toInt()} / ${ride.maxSpeedKmh.toInt()} km/h",
                    highlightColor = MaterialTheme.colorScheme.secondary
                )
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(SubtleGray.copy(alpha = 0.3f)))
                MetricColumn(
                    label = "MAX BRAKE",
                    value = String.format(Locale.US, "%.2fg", ride.maxBrakeG),
                    highlightColor = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Export Actions (GPX & CSV)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportGpx,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("EXPORT GPX", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExportCsv,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("EXPORT CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    highlightColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SubtleGray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = highlightColor,
            fontWeight = FontWeight.Bold
        )
    }
}
