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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.mototags.apextelemetry.ui.components.GForceMeter
import com.mototags.apextelemetry.ui.components.LeanGauge
import com.mototags.apextelemetry.ui.components.Speedometer
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.DarkSurface
import com.mototags.apextelemetry.ui.theme.NeonCyan
import com.mototags.apextelemetry.ui.theme.NeonGreen
import com.mototags.apextelemetry.ui.theme.PureBlack
import com.mototags.apextelemetry.ui.theme.SubtleGray

@Composable
fun HUDDashboardScreen(
    viewModel: TelemetryViewModel,
    modifier: Modifier = Modifier
) {
    val telemetryData by viewModel.telemetryState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
    ) {
        if (isLandscape) {
            LandscapeLayout(
                data = telemetryData,
                onCalibrate = { viewModel.calibrateZero() },
                onReset = { viewModel.resetPeaks() }
            )
        } else {
            PortraitLayout(
                data = telemetryData,
                onCalibrate = { viewModel.calibrateZero() },
                onReset = { viewModel.resetPeaks() }
            )
        }
    }
}

@Composable
private fun PortraitLayout(
    data: com.mototags.apextelemetry.model.TelemetryData,
    onCalibrate: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        TopBar(isCalibrated = data.isCalibrated, onReset = onReset)

        Spacer(modifier = Modifier.height(16.dp))

        // Center Lean Gauge
        LeanGauge(
            currentLean = data.currentLeanAngle,
            maxLeft = data.maxLeftLean,
            maxRight = data.maxRightLean,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Lower Instruments (Speed & G-Force)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(16.dp),
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
                    .height(110.dp)
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

        Spacer(modifier = Modifier.height(24.dp))

        // Glove-Friendly Tare Calibration Button
        CalibrateButton(
            isCalibrated = data.isCalibrated,
            onClick = onCalibrate,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        )
    }
}

@Composable
private fun LandscapeLayout(
    data: com.mototags.apextelemetry.model.TelemetryData,
    onCalibrate: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Speedometer & G-Force Radar
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (data.isCalibrated) "● ZEROED" else "○ UNTARED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (data.isCalibrated) NeonGreen else SubtleGray
                )

                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SubtleGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Peaks", modifier = Modifier.padding(end = 4.dp))
                    Text("RESET", fontSize = 12.sp)
                }
            }

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

            CalibrateButton(
                isCalibrated = data.isCalibrated,
                onClick = onCalibrate,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TopBar(
    isCalibrated: Boolean,
    onReset: () -> Unit
) {
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
            Text(
                text = if (isCalibrated) "STATUS: CALIBRATED (0° SET)" else "STATUS: MOUNT & TARE ZERO",
                style = MaterialTheme.typography.labelSmall,
                color = if (isCalibrated) NeonGreen else SubtleGray
            )
        }

        OutlinedButton(
            onClick = onReset,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SubtleGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.padding(end = 4.dp))
            Text("RESET", fontSize = 12.sp)
        }
    }
}

@Composable
private fun CalibrateButton(
    isCalibrated: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCalibrated) DarkCard else NeonCyan,
            contentColor = if (isCalibrated) NeonCyan else PureBlack
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.CropRotate,
            contentDescription = "Calibrate Zero",
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = if (isCalibrated) "RE-TARE ZERO POINT" else "CALIBRATE / TARE ZERO",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
