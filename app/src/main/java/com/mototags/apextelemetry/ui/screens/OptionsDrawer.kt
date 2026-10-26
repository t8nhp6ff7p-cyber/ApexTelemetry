package com.mototags.apextelemetry.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.model.DisplayStyle
import com.mototags.apextelemetry.model.LeanScale
import com.mototags.apextelemetry.model.SpeedUnit
import com.mototags.apextelemetry.model.TelemetryData
import com.mototags.apextelemetry.ui.TelemetryViewModel
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.DarkSurface
import com.mototags.apextelemetry.ui.theme.NeonGreen
import com.mototags.apextelemetry.ui.theme.NeonOrange
import com.mototags.apextelemetry.ui.theme.PureBlack
import com.mototags.apextelemetry.ui.theme.SubtleGray

@Composable
fun OptionsDrawerContent(
    data: TelemetryData,
    viewModel: TelemetryViewModel,
    onClose: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(340.dp)
            .background(DarkSurface)
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp).padding(end = 8.dp)
                )
                Text(
                    text = "SETTINGS & COCKPIT",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: MANUAL CALIBRATION (TARE ZERO)
        Text(
            text = "MANUAL CALIBRATION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (data.isCalibrated) "STATUS: CALIBRATED (0° SET)" else "STATUS: UNCALIBRATED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (data.isCalibrated) NeonGreen else NeonOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sit on bike, hold it upright off kickstand, and tap Calibrate to lock your 0° horizon.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = SubtleGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.calibrateManual() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.CropRotate, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(end = 6.dp))
                    Text(
                        text = if (data.isCalibrated) "RE-TARE HORIZON (0°)" else "TARE / CALIBRATE ZERO",
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: GAUGE CLUSTER SELECTION
        Text(
            text = "INSTRUMENT CLUSTER STYLE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        DisplayStyle.values().forEach { style ->
            val isSelected = style == data.displayStyle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else DarkCard)
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.setDisplayStyle(style) }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = style.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = style.subtitle, style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                    }
                    if (isSelected) {
                        Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: MAX LEAN ANGLE SCALE (45° / 55° / 65°)
        Text(
            text = "MAX LEAN ANGLE SCALE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeanScale.values().forEach { scale ->
                val isSelected = scale == data.leanScale
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else DarkCard)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.setLeanScale(scale) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${scale.maxDegrees}°",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: SPEED UNITS & DIRECTION
        Text(
            text = "UNITS & PHYSICS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Speed Unit", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Row {
                SpeedUnit.values().forEach { unit ->
                    val isSel = unit == data.speedUnit
                    Text(
                        text = unit.label,
                        color = if (isSel) MaterialTheme.colorScheme.primary else SubtleGray,
                        fontWeight = if (isSel) FontWeight.Black else FontWeight.Normal,
                        modifier = Modifier
                            .clickable { viewModel.setSpeedUnit(unit) }
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Invert Roll Direction", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(if (data.isInvertedDirection) "Left is +" else "Right is +", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
            }
            Switch(
                checked = data.isInvertedDirection,
                onCheckedChange = { viewModel.toggleDirectionInvert() },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: THEMES
        ThemeSelectorRow(
            currentTheme = data.selectedTheme,
            onSelectTheme = { viewModel.setTheme(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 6: SAVED RIDES LOGBOOK BUTTON
        OutlinedButton(
            onClick = {
                onClose()
                onOpenHistory()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("OPEN RIDES LOGBOOK", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
