package com.mototags.apextelemetry.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.ui.theme.ArcTrackColor
import com.mototags.apextelemetry.ui.theme.NeonRed
import com.mototags.apextelemetry.ui.theme.SubtleGray
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LeanGauge(
    currentLean: Float,
    maxLeft: Float,
    maxRight: Float,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    val animatedLean by animateFloatAsState(
        targetValue = currentLean.coerceIn(-65f, 65f),
        animationSpec = spring(stiffness = 850f),
        label = "LeanAngleAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Peak Indicators Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "MAX LEFT",
                    style = MaterialTheme.typography.labelSmall,
                    color = SubtleGray
                )
                Text(
                    text = "${maxLeft.toInt()}°",
                    style = MaterialTheme.typography.titleLarge,
                    color = secondaryColor,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "MAX RIGHT",
                    style = MaterialTheme.typography.labelSmall,
                    color = SubtleGray
                )
                Text(
                    text = "${maxRight.toInt()}°",
                    style = MaterialTheme.typography.titleLarge,
                    color = secondaryColor,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center Arc Gauge Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp, 160.dp)
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                val strokeWidth = 18.dp.toPx()
                val radius = (size.width - strokeWidth) / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                val startAngle = 210f
                val sweepTotal = 120f
                val zeroAngle = 270f

                // Track Background
                drawArc(
                    color = ArcTrackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Tick marks at 15°, 30°, 45°
                for (tickDeg in listOf(-45, -30, -15, 15, 30, 45)) {
                    val angle = zeroAngle + tickDeg
                    val rad = Math.toRadians(angle.toDouble())
                    val tStart = Offset(
                        centerOffset.x + (radius - strokeWidth / 2) * cos(rad).toFloat(),
                        centerOffset.y + (radius - strokeWidth / 2) * sin(rad).toFloat()
                    )
                    val tEnd = Offset(
                        centerOffset.x + (radius + strokeWidth / 2) * cos(rad).toFloat(),
                        centerOffset.y + (radius + strokeWidth / 2) * sin(rad).toFloat()
                    )
                    drawLine(
                        color = SubtleGray.copy(alpha = 0.5f),
                        start = tStart,
                        end = tEnd,
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Center Zero Notch
                val zeroRad = Math.toRadians(zeroAngle.toDouble())
                val zeroStart = Offset(
                    centerOffset.x + (radius - strokeWidth / 2) * cos(zeroRad).toFloat(),
                    centerOffset.y + (radius - strokeWidth / 2) * sin(zeroRad).toFloat()
                )
                val zeroEnd = Offset(
                    centerOffset.x + (radius + strokeWidth / 2) * cos(zeroRad).toFloat(),
                    centerOffset.y + (radius + strokeWidth / 2) * sin(zeroRad).toFloat()
                )
                drawLine(
                    color = Color.White,
                    start = zeroStart,
                    end = zeroEnd,
                    strokeWidth = 3.dp.toPx()
                )

                // Live Lean Arc
                val leanColor = when {
                    abs(animatedLean) > 52f -> NeonRed
                    abs(animatedLean) > 38f -> secondaryColor
                    else -> primaryColor
                }

                drawArc(
                    color = leanColor,
                    startAngle = zeroAngle,
                    sweepAngle = animatedLean,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Left Peak Notch
                if (maxLeft > 0f) {
                    val peakLeftAngle = zeroAngle - maxLeft.coerceAtMost(60f)
                    val rad = Math.toRadians(peakLeftAngle.toDouble())
                    val pStart = Offset(
                        centerOffset.x + (radius - strokeWidth / 2) * cos(rad).toFloat(),
                        centerOffset.y + (radius - strokeWidth / 2) * sin(rad).toFloat()
                    )
                    val pEnd = Offset(
                        centerOffset.x + (radius + strokeWidth / 2) * cos(rad).toFloat(),
                        centerOffset.y + (radius + strokeWidth / 2) * sin(rad).toFloat()
                    )
                    drawLine(color = secondaryColor, start = pStart, end = pEnd, strokeWidth = 4.dp.toPx())
                }

                // Right Peak Notch
                if (maxRight > 0f) {
                    val peakRightAngle = zeroAngle + maxRight.coerceAtMost(60f)
                    val rad = Math.toRadians(peakRightAngle.toDouble())
                    val pStart = Offset(
                        centerOffset.x + (radius - strokeWidth / 2) * cos(rad).toFloat(),
                        centerOffset.y + (radius - strokeWidth / 2) * sin(rad).toFloat()
                    )
                    val pEnd = Offset(
                        centerOffset.x + (radius + strokeWidth / 2) * cos(rad).toFloat(),
                        centerOffset.y + (radius + strokeWidth / 2) * sin(rad).toFloat()
                    )
                    drawLine(color = secondaryColor, start = pStart, end = pEnd, strokeWidth = 4.dp.toPx())
                }
            }

            // Central Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 42.dp)
            ) {
                val leanAbs = abs(animatedLean).toInt()
                val direction = when {
                    animatedLean < -1.5f -> "◄ LEAN L"
                    animatedLean > 1.5f -> "LEAN R ►"
                    else -> "UPRIGHT"
                }

                Text(
                    text = "$leanAbs°",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 62.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = direction,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (leanAbs > 38) secondaryColor else primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
