package com.mototags.apextelemetry.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.model.RideState
import com.mototags.apextelemetry.model.TelemetryData
import com.mototags.apextelemetry.ui.components.GForceMeter
import com.mototags.apextelemetry.ui.components.LeanGauge
import com.mototags.apextelemetry.ui.components.Speedometer
import com.mototags.apextelemetry.ui.screens.RideSummaryDialog
import com.mototags.apextelemetry.ui.screens.SavedRidesScreen
import com.mototags.apextelemetry.ui.screens.ThemeSelectorRow
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.DarkSurface
import com.mototags.apextelemetry.ui.theme.NeonGreen
import com.mototags.apextelemetry.ui.theme.NeonRed
import com.mototags.apextelemetry.ui.theme.PureBlack
import com.mototags.apextelemetry.ui.theme.SubtleGray
import java.util.Locale

@Composable
fun HUDDashboardScreen(
    viewModel: TelemetryViewModel,
    modifier: Modifier = Modifier
) {
    val telemetryData by viewModel.telemetryState.collectAsState()
    val savedRides by viewModel.savedRides.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Navigation Tab Bar (Cockpit vs Saved Rides)
            if (telemetryData.rideState != RideState.RIDING) {
                TabRow(
                    selectedTabIndex = telemetryData.activeTab,
                    containerColor = DarkSurface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[telemetryData.activeTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = telemetryData.activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text("COCKPIT HUD", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = telemetryData.activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text("SAVED RIDES (${savedRides.size})", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }

            if (telemetryData.activeTab == 1 && telemetryData.rideState != RideState.RIDING) {
                SavedRidesScreen(
                    rides = savedRides,
                    onDeleteRide = { viewModel.deleteRide(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Cockpit HUD Screen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    if (isLandscape) {
                        LandscapeCockpit(
                            data = telemetryData,
                            viewModel = viewModel
                        )
                    } else {
                        PortraitCockpit(
                            data = telemetryData,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Ride Summary Dialog
        if (telemetryData.rideState == RideState.SUMMARY) {
            RideSummaryDialog(
                data = telemetryData,
                onSave = { title -> viewModel.saveCurrentRide(title) },
                onDiscard = { viewModel.discardRide() }
            )
        }
    }
}

@Composable
private fun PortraitCockpit(
    data: TelemetryData,
    viewModel: TelemetryViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cockpit Header with Ride Timer / Status
        CockpitHeader(data = data)

        // Center Lean Gauge
        LeanGauge(
            currentLean = data.currentLeanAngle,
            maxLeft = data.maxLeftLean,
            maxRight = data.maxRightLean,
            primaryColor = MaterialTheme.colorScheme.primary,
            secondaryColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth()
        )

        // Speed & G-Force Instruments
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Speedometer(
                speedKmh = data.speedKmh,
                maxSpeedKmh = data.maxSpeedKmh,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(90.dp)
                    .background(DarkCard)
            )

            GForceMeter(
                lateralG = data.lateralG,
                longitudinalG = data.longitudinalG,
                maxLateralG = data.maxLateralG,
                maxBrakeG = data.maxBrakeG,
                modifier = Modifier.weight(1f)
            )
        }

        // Action Buttons / Ride Controls
        when (data.rideState) {
            RideState.READY -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Theme Selector Row
                    ThemeSelectorRow(
                        currentTheme = data.selectedTheme,
                        onSelectTheme = { viewModel.setTheme(it) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Large Start Ride Button
                    Button(
                        onClick = { viewModel.startRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(58.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(end = 8.dp))
                        Text("START RIDE (AUTO-CALIBRATE)", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hold bike upright off kickstand and press Start. It will tare 0° and start tracking.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SubtleGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            RideState.RIDING -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.pauseRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 6.dp))
                        Text("PAUSE", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.finishRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 6.dp))
                        Text("FINISH RIDE", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            RideState.PAUSED -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.resumeRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(end = 6.dp))
                        Text("RESUME", fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Button(
                        onClick = { viewModel.finishRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 6.dp))
                        Text("FINISH", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            RideState.SUMMARY -> {}
        }
    }
}

@Composable
private fun LandscapeCockpit(
    data: TelemetryData,
    viewModel: TelemetryViewModel
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Instruments and Ride Controls
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CockpitHeader(data = data)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Speedometer(
                    speedKmh = data.speedKmh,
                    maxSpeedKmh = data.maxSpeedKmh,
                    modifier = Modifier.weight(1f)
                )

                GForceMeter(
                    lateralG = data.lateralG,
                    longitudinalG = data.longitudinalG,
                    maxLateralG = data.maxLateralG,
                    maxBrakeG = data.maxBrakeG,
                    modifier = Modifier.weight(1f)
                )
            }

            // Controls
            if (data.rideState == RideState.READY) {
                Button(
                    onClick = { viewModel.startRide() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(end = 6.dp))
                    Text("START RIDE (AUTO-CALIBRATE)", fontWeight = FontWeight.Black, color = Color.Black)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (data.rideState == RideState.RIDING) {
                        Button(
                            onClick = { viewModel.pauseRide() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("PAUSE", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.resumeRide() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("RESUME", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Button(
                        onClick = { viewModel.finishRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("FINISH", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Column: Lean Gauge
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            LeanGauge(
                currentLean = data.currentLeanAngle,
                maxLeft = data.maxLeftLean,
                maxRight = data.maxRightLean,
                primaryColor = MaterialTheme.colorScheme.primary,
                secondaryColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CockpitHeader(data: TelemetryData) {
    val minutes = data.rideDurationSeconds / 60
    val seconds = data.rideDurationSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "APEX TELEMETRY",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            val statusColor = when (data.rideState) {
                RideState.RIDING -> NeonGreen
                RideState.PAUSED -> MaterialTheme.colorScheme.secondary
                else -> SubtleGray
            }
            Text(
                text = when (data.rideState) {
                    RideState.RIDING -> "● RECORDING LIVE"
                    RideState.PAUSED -> "❚❚ PAUSED"
                    else -> if (data.isCalibrated) "● ZEROED (READY)" else "○ READY TO MOUNT"
                },
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }

        if (data.rideState != RideState.READY) {
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}
