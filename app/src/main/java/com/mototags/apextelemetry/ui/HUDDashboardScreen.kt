package com.mototags.apextelemetry.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.mototags.apextelemetry.ui.components.SensorHealthBar
import com.mototags.apextelemetry.ui.components.Speedometer
import com.mototags.apextelemetry.ui.screens.OptionsDrawerContent
import com.mototags.apextelemetry.ui.screens.RecoveryDialog
import com.mototags.apextelemetry.ui.screens.RideSummaryDialog
import com.mototags.apextelemetry.ui.screens.SavedRidesScreen
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.DarkSurface
import com.mototags.apextelemetry.ui.theme.NeonGreen
import com.mototags.apextelemetry.ui.theme.NeonOrange
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
    var showSavedRidesScreen by remember { mutableStateOf(false) }
    var selectedRideForAnalysis by remember { mutableStateOf<com.mototags.apextelemetry.model.RideSession?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        if (selectedRideForAnalysis != null) {
            com.mototags.apextelemetry.ui.screens.PostRideAnalysisScreen(
                ride = selectedRideForAnalysis!!,
                onBack = { selectedRideForAnalysis = null },
                onExportGpx = { viewModel.exportRideGpx(selectedRideForAnalysis!!) },
                onExportKml = { viewModel.exportRideKml(selectedRideForAnalysis!!) },
                onExportCsv = { viewModel.exportRideCsv(selectedRideForAnalysis!!) }
            )
        } else if (showSavedRidesScreen) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RIDES LOGBOOK & EXPORTS",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    OutlinedButton(
                        onClick = { showSavedRidesScreen = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("BACK TO COCKPIT", fontWeight = FontWeight.Bold)
                    }
                }
                SavedRidesScreen(
                    rides = savedRides,
                    onSelectRide = { selectedRideForAnalysis = it },
                    onDeleteRide = { viewModel.deleteRide(it) },
                    onExportGpx = { viewModel.exportRideGpx(it) },
                    onExportCsv = { viewModel.exportRideCsv(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Cockpit Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Cockpit Header Bar
                CockpitTopBar(
                    data = telemetryData,
                    onOpenMenu = { viewModel.setDrawerOpen(true) },
                    onManualTare = { viewModel.calibrateManual() }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Pillar 1: Sensor Health & GPS Quality Bar
                SensorHealthBar(
                    health = telemetryData.sensorHealth,
                    tripDistanceKm = telemetryData.tripDistanceKm,
                    speedUnit = telemetryData.speedUnit
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Center Instruments
                Box(modifier = Modifier.weight(1f)) {
                    if (isLandscape) {
                        LandscapeCockpit(data = telemetryData, viewModel = viewModel)
                    } else {
                        PortraitCockpit(data = telemetryData, viewModel = viewModel)
                    }
                }
            }
        }

        // Sliding Options Drawer
        AnimatedVisibility(
            visible = telemetryData.isDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { viewModel.setDrawerOpen(false) }
            ) {
                OptionsDrawerContent(
                    data = telemetryData,
                    viewModel = viewModel,
                    onClose = { viewModel.setDrawerOpen(false) },
                    onOpenHistory = { showSavedRidesScreen = true },
                    modifier = Modifier.clickable(enabled = false) {}
                )
            }
        }

        // Pillar 2: Crash Recovery Dialog (Write-Ahead Log)
        telemetryData.recoveredRide?.let { recovered ->
            RecoveryDialog(
                recoveredRide = recovered,
                onSave = {
                    viewModel.saveRecoveredRide()
                    showSavedRidesScreen = true
                },
                onDismiss = { viewModel.dismissRecovery() }
            )
        }

        // Ride Summary Dialog
        if (telemetryData.showSummaryDialog) {
            RideSummaryDialog(
                data = telemetryData,
                onSave = { title ->
                    viewModel.saveCurrentRide(title)
                    showSavedRidesScreen = true
                },
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
        // Selected Gauge Cluster
        Box(
            modifier = Modifier.weight(1.3f),
            contentAlignment = Alignment.Center
        ) {
            when (data.displayStyle) {
                DisplayStyle.ANALOG_DIAL -> {
                    AnalogDialGauge(
                        currentLean = data.currentLeanAngle,
                        maxLeft = data.maxLeftLean,
                        maxRight = data.maxRightLean,
                        leanScale = data.leanScale,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DisplayStyle.DIGITAL_MOTO -> {
                    LeanGauge(
                        currentLean = data.currentLeanAngle,
                        maxLeft = data.maxLeftLean,
                        maxRight = data.maxRightLean,
                        leanScale = data.leanScale,
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
                        avgSpeedKmh = data.avgSpeedKmh,
                        leanScale = data.leanScale,
                        speedUnit = data.speedUnit,
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
                        speedUnit = data.speedUnit,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Sub Instruments (Speed & G-Force Radar)
        if (data.displayStyle == DisplayStyle.ANALOG_DIAL || data.displayStyle == DisplayStyle.DIGITAL_MOTO) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(16.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Speedometer(
                    speedKmh = data.speedKmh,
                    maxSpeedKmh = data.maxSpeedKmh,
                    avgSpeedKmh = data.avgSpeedKmh,
                    speedUnit = data.speedUnit,
                    modifier = Modifier.weight(1f)
                )

                Box(modifier = Modifier.width(1.dp).height(85.dp).background(DarkCard))

                GForceMeter(
                    lateralG = data.lateralG,
                    longitudinalG = data.longitudinalG,
                    maxLateralG = data.maxLateralG,
                    maxBrakeG = data.maxBrakeG,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Ride Controls
        when (data.rideState) {
            RideState.READY -> {
                Button(
                    onClick = { viewModel.startRide() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(end = 8.dp))
                    Text("START RIDE SESSION", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
            RideState.RIDING -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
        // Left Column: Instruments & Ride Controls
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Speedometer(
                    speedKmh = data.speedKmh,
                    maxSpeedKmh = data.maxSpeedKmh,
                    avgSpeedKmh = data.avgSpeedKmh,
                    speedUnit = data.speedUnit,
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
                Button(
                    onClick = { viewModel.startRide() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(end = 6.dp))
                    Text("START RIDE SESSION", fontWeight = FontWeight.Black, color = Color.Black)
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
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("PAUSE", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.resumeRide() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("RESUME", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Button(
                        onClick = { viewModel.finishRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("FINISH", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right Column: Active Gauge Cluster
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            when (data.displayStyle) {
                DisplayStyle.ANALOG_DIAL -> {
                    AnalogDialGauge(
                        currentLean = data.currentLeanAngle,
                        maxLeft = data.maxLeftLean,
                        maxRight = data.maxRightLean,
                        leanScale = data.leanScale,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DisplayStyle.DIGITAL_MOTO -> {
                    LeanGauge(
                        currentLean = data.currentLeanAngle,
                        maxLeft = data.maxLeftLean,
                        maxRight = data.maxRightLean,
                        leanScale = data.leanScale,
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
                        avgSpeedKmh = data.avgSpeedKmh,
                        leanScale = data.leanScale,
                        speedUnit = data.speedUnit,
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
                        speedUnit = data.speedUnit,
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
private fun CockpitTopBar(
    data: TelemetryData,
    onOpenMenu: () -> Unit,
    onManualTare: () -> Unit
) {
    val minutes = data.rideDurationSeconds / 60
    val seconds = data.rideDurationSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenMenu) {
                Icon(Icons.Default.Menu, contentDescription = "Menu Options", tint = Color.White)
            }

            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "APEX COCKPIT",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                val statusText = when (data.rideState) {
                    RideState.RIDING -> "● LIVE $timeFormatted"
                    RideState.PAUSED -> "❚❚ PAUSED $timeFormatted"
                    else -> if (data.isCalibrated) "● ZEROED (${data.leanScale.maxDegrees}° SCALE)" else "○ READY TO TARE"
                }
                val statusColor = when (data.rideState) {
                    RideState.RIDING -> NeonGreen
                    RideState.PAUSED -> NeonOrange
                    else -> if (data.isCalibrated) NeonGreen else SubtleGray
                }
                Text(text = statusText, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
            }
        }

        // Manual Tare Horizon Quick Button
        OutlinedButton(
            onClick = onManualTare,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (data.isCalibrated) NeonGreen else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.CropRotate, contentDescription = "Tare Zero", modifier = Modifier.size(16.dp).padding(end = 4.dp))
            Text(if (data.isCalibrated) "ZEROED" else "TARE ZERO", fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}
