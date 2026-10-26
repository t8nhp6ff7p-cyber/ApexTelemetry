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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.mototags.apextelemetry.model.DisplayStyle
import com.mototags.apextelemetry.model.RideState
import com.mototags.apextelemetry.model.TelemetryData
import com.mototags.apextelemetry.ui.components.AnalogDialGauge
import com.mototags.apextelemetry.ui.components.GForceMeter
import com.mototags.apextelemetry.ui.components.HUDMirrorView
import com.mototags.apextelemetry.ui.components.LeanGauge
import com.mototags.apextelemetry.ui.components.RallyTwinGauge
import com.mototags.apextelemetry.ui.components.Speedometer
import com.mototags.apextelemetry.ui.screens.DisplayStyleSelector
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp)
                ) {
                    if (isLandscape) {
                        LandscapeCockpit(data = telemetryData, viewModel = viewModel)
                    } else {
                        PortraitCockpit(data = telemetryData, viewModel = viewModel)
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
        // Cockpit Header
        CockpitHeader(data = data, onInvert = { viewModel.toggleDirectionInvert() })

        // Central Dynamic Gauge Cluster (Based on Selected Display Style)
        when (data.displayStyle) {
            DisplayStyle.DIGITAL_MOTO -> {
                LeanGauge(
                    currentLean = data.currentLeanAngle,
                    maxLeft = data.maxLeftLean,
                    maxRight = data.maxRightLean,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            DisplayStyle.ANALOG_DIAL -> {
                AnalogDialGauge(
                    currentLean = data.currentLeanAngle,
                    maxLeft = data.maxLeftLean,
                    maxRight = data.maxRightLean,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            DisplayStyle.RALLY_TWIN -> {
                RallyTwinGauge(
                    currentLean = data.currentLeanAngle,
                    maxLeft = data.maxLeftLean,
                    maxRight = data.maxRightLean,
                    speedKmh = data.speedKmh,
                    maxSpeedKmh = data.maxSpeedKmh,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            DisplayStyle.HUD_MIRROR -> {
                HUDMirrorView(
                    currentLean = data.currentLeanAngle,
                    speedKmh = data.speedKmh,
                    maxLeft = data.maxLeftLean,
                    maxRight = data.maxRightLean,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sub Instruments (Speed & G-Force, shown when not in Rally or HUD Mirror)
        if (data.displayStyle == DisplayStyle.DIGITAL_MOTO || data.displayStyle == DisplayStyle.ANALOG_DIAL) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Speedometer(
                    speedKmh = data.speedKmh,
                    maxSpeedKmh = data.maxSpeedKmh,
                    modifier = Modifier.weight(1f)
                )

                Box(modifier = Modifier.width(1.dp).height(80.dp).background(DarkCard))

                GForceMeter(
                    lateralG = data.lateralG,
                    longitudinalG = data.longitudinalG,
                    maxLateralG = data.maxLateralG,
                    maxBrakeG = data.maxBrakeG,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Controls & Options
        when (data.rideState) {
            RideState.READY -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Gauge Cluster Style Selector (Speedometer 55 style)
                    DisplayStyleSelector(
                        currentStyle = data.displayStyle,
                        onSelectStyle = { viewModel.setDisplayStyle(it) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Theme Selector
                    ThemeSelectorRow(
                        currentTheme = data.selectedTheme,
                        onSelectTheme = { viewModel.setTheme(it) },
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Start Ride Button
                    Button(
                        onClick = { viewModel.startRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(end = 8.dp))
                        Text("START RIDE (AUTO-CALIBRATE)", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }
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
                        modifier = Modifier.weight(1f).height(54.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 6.dp))
                        Text("PAUSE", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.finishRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(54.dp)
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
                        modifier = Modifier.weight(1f).height(54.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(end = 6.dp))
                        Text("RESUME", fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Button(
                        onClick = { viewModel.finishRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(54.dp)
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
        // Left Column: Instruments & Controls
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CockpitHeader(data = data, onInvert = { viewModel.toggleDirectionInvert() })

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

            if (data.rideState == RideState.READY) {
                Column {
                    DisplayStyleSelector(
                        currentStyle = data.displayStyle,
                        onSelectStyle = { viewModel.setDisplayStyle(it) },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Button(
                        onClick = { viewModel.startRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(end = 6.dp))
                        Text("START RIDE (AUTO-CALIBRATE)", fontWeight = FontWeight.Black, color = Color.Black)
                    }
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

        Spacer(modifier = Modifier.width(14.dp))

        // Right Column: Display Cluster
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            when (data.displayStyle) {
                DisplayStyle.DIGITAL_MOTO -> {
                    LeanGauge(
                        currentLean = data.currentLeanAngle,
                        maxLeft = data.maxLeftLean,
                        maxRight = data.maxRightLean,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DisplayStyle.ANALOG_DIAL -> {
                    AnalogDialGauge(
                        currentLean = data.currentLeanAngle,
                        maxLeft = data.maxLeftLean,
                        maxRight = data.maxRightLean,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DisplayStyle.RALLY_TWIN -> {
                    RallyTwinGauge(
                        currentLean = data.currentLeanAngle,
                        maxLeft = data.maxLeftLean,
                        maxRight = data.maxRightLean,
                        speedKmh = data.speedKmh,
                        maxSpeedKmh = data.maxSpeedKmh,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DisplayStyle.HUD_MIRROR -> {
                    HUDMirrorView(
                        currentLean = data.currentLeanAngle,
                        speedKmh = data.speedKmh,
                        maxLeft = data.maxLeftLean,
                        maxRight = data.maxRightLean,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun CockpitHeader(
    data: TelemetryData,
    onInvert: () -> Unit
) {
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
                    else -> if (data.isCalibrated) "● ZEROED" else "○ READY"
                },
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Direction Invert Button
            OutlinedButton(
                onClick = onInvert,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (data.isInvertedDirection) MaterialTheme.colorScheme.primary else SubtleGray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = "Invert Direction", modifier = Modifier.padding(end = 4.dp))
                Text(if (data.isInvertedDirection) "DIR: INVERTED" else "DIR: NORMAL", fontSize = 11.sp)
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
}
