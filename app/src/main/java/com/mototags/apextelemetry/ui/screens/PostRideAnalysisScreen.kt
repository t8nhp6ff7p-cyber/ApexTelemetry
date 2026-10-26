package com.mototags.apextelemetry.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.model.RideSession
import com.mototags.apextelemetry.model.TelemetryPoint
import com.mototags.apextelemetry.ui.theme.ArcTrackColor
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.DarkSurface
import com.mototags.apextelemetry.ui.theme.NeonGreen
import com.mototags.apextelemetry.ui.theme.NeonOrange
import com.mototags.apextelemetry.ui.theme.NeonRed
import com.mototags.apextelemetry.ui.theme.PureBlack
import com.mototags.apextelemetry.ui.theme.SubtleGray
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun PostRideAnalysisScreen(
    ride: RideSession,
    onBack: () -> Unit,
    onExportGpx: () -> Unit,
    onExportKml: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val points = ride.points

    var scrubberProgress by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }

    // Auto-replay animator
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(50L)
            scrubberProgress += 0.005f
            if (scrubberProgress >= 1f) {
                scrubberProgress = 0f
                isPlaying = false
            }
        }
    }

    val currentIndex = if (points.isNotEmpty()) {
        (scrubberProgress * (points.size - 1)).toInt().coerceIn(0, points.size - 1)
    } else 0
    val activePoint: TelemetryPoint? = points.getOrNull(currentIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "RIDE INTELLIGENCE",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = ride.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = SubtleGray
                    )
                }
            }

            // Smoothness Score Badge
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SCORE ${ride.intelligence.smoothnessScore}/100",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 1: ROUTE PATH REPLAY CANVAS
        Text(
            text = "GPS ROUTE REPLAY & CORNER TRACE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // 2D Normalized Track Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (points.size >= 2) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            // Find bounding box of lat/lon
                            val validPts = points.filter { it.latitude != 0.0 && it.longitude != 0.0 }
                            if (validPts.size >= 2) {
                                val minLat = validPts.minOf { it.latitude }
                                val maxLat = validPts.maxOf { it.latitude }
                                val minLon = validPts.minOf { it.longitude }
                                val maxLon = validPts.maxOf { it.longitude }

                                val deltaLat = (maxLat - minLat).coerceAtLeast(0.0001)
                                val deltaLon = (maxLon - minLon).coerceAtLeast(0.0001)

                                val w = size.width
                                val h = size.height

                                // Draw path
                                for (i in 0 until validPts.size - 1) {
                                    val p1 = validPts[i]
                                    val p2 = validPts[i + 1]

                                    val x1 = ((p1.longitude - minLon) / deltaLon * w).toFloat()
                                    val y1 = (h - (p1.latitude - minLat) / deltaLat * h).toFloat()
                                    val x2 = ((p2.longitude - minLon) / deltaLon * w).toFloat()
                                    val y2 = (h - (p2.latitude - minLat) / deltaLat * h).toFloat()

                                    val segColor = when {
                                        abs(p1.leanAngle) > 35f -> NeonRed
                                        abs(p1.leanAngle) > 20f -> NeonOrange
                                        else -> NeonGreen.copy(alpha = 0.6f)
                                    }

                                    drawLine(
                                        color = segColor,
                                        start = Offset(x1, y1),
                                        end = Offset(x2, y2),
                                        strokeWidth = 3.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }

                                // Draw active replay cursor
                                val curPt = validPts.getOrNull(currentIndex.coerceIn(0, validPts.size - 1))
                                if (curPt != null) {
                                    val cx = ((curPt.longitude - minLon) / deltaLon * w).toFloat()
                                    val cy = (h - (curPt.latitude - minLat) / deltaLat * h).toFloat()
                                    drawCircle(color = Color.White, radius = 7.dp.toPx(), center = Offset(cx, cy))
                                    drawCircle(color = NeonRed, radius = 4.dp.toPx(), center = Offset(cx, cy))
                                }
                            }
                        }
                    } else {
                        Text("No GPS Track Coordinates Logged", color = SubtleGray, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Replay Slider & Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = scrubberProgress,
                        onValueChange = {
                            scrubberProgress = it
                            isPlaying = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = ArcTrackColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Live Scrubber Telemetry Teleprinter
                activePoint?.let { pt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkCard, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TelemetryBadge(label = "SPEED", value = "${pt.speedKmh.toInt()} km/h")
                        TelemetryBadge(label = "LEAN", value = "${pt.leanAngle.toInt()}°")
                        TelemetryBadge(label = "LAT G", value = String.format(Locale.US, "%.2fg", pt.lateralG))
                        TelemetryBadge(label = "HEADING", value = "${pt.heading.toInt()}°")
                        TelemetryBadge(label = "ALT", value = "${pt.altitude.toInt()}m")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 2: TELEMETRY TRACE CHARTS (SPEED & LEAN OVER TIME)
        Text(
            text = "SYNCHRONIZED TELEMETRY TRACE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Speed (km/h) & Lean Angle Trace", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color.Black, RoundedCornerShape(10.dp))
                ) {
                    if (points.size >= 2) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp)) {
                            val w = size.width
                            val h = size.height
                            val maxSpd = (points.maxOfOrNull { it.speedKmh } ?: 120f).coerceAtLeast(40f)

                            val stepX = w / (points.size - 1)

                            // Speed trace path (White)
                            val speedPath = Path()
                            for (i in points.indices) {
                                val x = i * stepX
                                val y = h - (points[i].speedKmh / maxSpd * h)
                                if (i == 0) speedPath.moveTo(x, y) else speedPath.lineTo(x, y)
                            }
                            drawPath(path = speedPath, color = Color.White, style = Stroke(width = 2.dp.toPx()))

                            // Lean trace path (Neon Cyan)
                            val leanPath = Path()
                            for (i in points.indices) {
                                val x = i * stepX
                                val normLean = (points[i].leanAngle / 60f).coerceIn(-1f, 1f)
                                val y = (h / 2f) - (normLean * (h / 2.5f))
                                if (i == 0) leanPath.moveTo(x, y) else leanPath.lineTo(x, y)
                            }
                            drawPath(path = leanPath, color = NeonOrange, style = Stroke(width = 1.8.dp.toPx()))

                            // Scrubber Vertical Line
                            val curX = (currentIndex.toFloat() / (points.size - 1)) * w
                            drawLine(color = Color.Red, start = Offset(curX, 0f), end = Offset(curX, h), strokeWidth = 1.5.dp.toPx())
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 3: CORNERING & ELEVATION INTELLIGENCE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Corners Breakdown
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(" CORNERS", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("LEFT CORNERS: ${ride.intelligence.totalLeftCorners}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text("RIGHT CORNERS: ${ride.intelligence.totalRightCorners}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text("TOTAL: ${ride.intelligence.totalLeftCorners + ride.intelligence.totalRightCorners}", style = MaterialTheme.typography.labelSmall, color = NeonOrange, fontWeight = FontWeight.Bold)
                }
            }

            // Elevation Breakdown
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Landscape, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Text(" ELEVATION", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("GAIN: +${ride.intelligence.elevationGainMeters.toInt()}m", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text("MIN ALT: ${ride.intelligence.minAltitudeMeters.toInt()}m", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                    Text("MAX ALT: ${ride.intelligence.maxAltitudeMeters.toInt()}m", style = MaterialTheme.typography.labelSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: EXPORT TOOLBAR (GPX, KML 3D, CSV)
        Text(
            text = "TELEMETRY EXPORT FORMATS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onExportGpx,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Text("GPX", color = Color.Black, fontWeight = FontWeight.Black)
            }

            Button(
                onClick = onExportKml,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Text("KML (3D)", color = Color.Black, fontWeight = FontWeight.Black)
            }

            Button(
                onClick = onExportCsv,
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Text("CSV", color = Color.White, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TelemetryBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SubtleGray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = Color.White, fontWeight = FontWeight.Bold)
    }
}
