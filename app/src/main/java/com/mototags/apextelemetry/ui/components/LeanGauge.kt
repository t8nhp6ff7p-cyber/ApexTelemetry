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
import com.mototags.apextelemetry.model.LeanScale
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
    leanScale: LeanScale,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    val maxLimit = leanScale.maxDegrees.toFloat()
    val animatedLean by animateFloatAsState(
        targetValue = currentLean.coerceIn(-maxLimit, maxLimit),
        animationSpec = spring(stiffness = 850f),
        label = "LeanAngleAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Peak Readouts Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "PEAK LEFT", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                Text(
                    text = "${maxLeft.toInt()}°",
                    style = MaterialTheme.typography.titleLarge,
                    color = secondaryColor,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "PEAK RIGHT", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                Text(
                    text = "${maxRight.toInt()}°",
                    style = MaterialTheme.typography.titleLarge,
                    color = secondaryColor,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                val scaleFactor = (sweepTotal / 2f) / maxLimit

                // Background Segmented Arc Track
                drawArc(
                    color = ArcTrackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Zero Center Notch
                val zeroRad = Math.toRadians(zeroAngle.toDouble())
                val zeroStart = Offset(
                    centerOffset.x + (radius - strokeWidth / 2) * cos(zeroRad).toFloat(),
                    centerOffset.y + (radius - strokeWidth / 2) * sin(zeroRad).toFloat()
                )
                val zeroEnd = Offset(
                    centerOffset.x + (radius + strokeWidth / 2) * cos(zeroRad).toFloat(),
                    centerOffset.y + (radius + strokeWidth / 2) * sin(zeroRad).toFloat()
                )
                drawLine(color = Color.White, start = zeroStart, end = zeroEnd, strokeWidth = 3.dp.toPx())

                // Dynamic Live Lean Sweep
                val leanSweepAngle = animatedLean * scaleFactor
                val leanColor = when {
                    abs(animatedLean) > (maxLimit * 0.85f) -> NeonRed
                    abs(animatedLean) > (maxLimit * 0.65f) -> secondaryColor
                    else -> primaryColor
                }

                drawArc(
                    color = leanColor,
                    startAngle = zeroAngle,
                    sweepAngle = leanSweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Left Peak Marker
                if (maxLeft > 0f) {
                    val pAngle = zeroAngle - (maxLeft.coerceAtMost(maxLimit) * scaleFactor)
                    val pRad = Math.toRadians(pAngle.toDouble())
                    val pStart = Offset(
                        centerOffset.x + (radius - strokeWidth / 2) * cos(pRad).toFloat(),
                        centerOffset.y + (radius - strokeWidth / 2) * sin(pRad).toFloat()
                    )
                    val pEnd = Offset(
                        centerOffset.x + (radius + strokeWidth / 2) * cos(pRad).toFloat(),
                        centerOffset.y + (radius + strokeWidth / 2) * sin(pRad).toFloat()
                    )
                    drawLine(color = secondaryColor, start = pStart, end = pEnd, strokeWidth = 4.dp.toPx())
                }

                // Right Peak Marker
                if (maxRight > 0f) {
                    val pAngle = zeroAngle + (maxRight.coerceAtMost(maxLimit) * scaleFactor)
                    val pRad = Math.toRadians(pAngle.toDouble())
                    val pStart = Offset(
                        centerOffset.x + (radius - strokeWidth / 2) * cos(pRad).toFloat(),
                        centerOffset.y + (radius - strokeWidth / 2) * sin(pRad).toFloat()
                    )
                    val pEnd = Offset(
                        centerOffset.x + (radius + strokeWidth / 2) * cos(pRad).toFloat(),
                        centerOffset.y + (radius + strokeWidth / 2) * sin(pRad).toFloat()
                    )
                    drawLine(color = secondaryColor, start = pStart, end = pEnd, strokeWidth = 4.dp.toPx())
                }
            }

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
                    color = if (leanAbs > (maxLimit * 0.65f)) secondaryColor else primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
