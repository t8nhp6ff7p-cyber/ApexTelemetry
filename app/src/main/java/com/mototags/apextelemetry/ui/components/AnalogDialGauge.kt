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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.model.LeanScale
import com.mototags.apextelemetry.ui.theme.ArcTrackColor
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.DarkSurface
import com.mototags.apextelemetry.ui.theme.NeonOrange
import com.mototags.apextelemetry.ui.theme.NeonRed
import com.mototags.apextelemetry.ui.theme.SubtleGray
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogDialGauge(
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
        label = "AnalogNeedle"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Peak Readouts Header
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

        Spacer(modifier = Modifier.height(6.dp))

        // High-End Skeuomorphic Gauge Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            Canvas(modifier = Modifier.size(270.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 8.dp.toPx()

                // Outer Bezel Rim (Dual layered brushed metallic styling)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(DarkSurface, Color(0xFF1F2430), Color(0xFF0F1217)),
                        center = center,
                        radius = radius + 6.dp.toPx()
                    ),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF2C3242),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = Color.Black,
                    radius = radius - 6.dp.toPx(),
                    center = center
                )

                // Zero is straight up (270 degrees in Android coordinate system)
                val zeroAngle = 270f
                // Total sweep angle on dial corresponds to maxLimit (e.g., ±55° = 110° total sweep, or scaled over 140°)
                val angleScale = 140f / (maxLimit * 2f)

                // Dial Face Graduations & Ticks
                val tickStep = if (leanScale == LeanScale.STREET_45) 5 else if (leanScale == LeanScale.SPORT_55) 5 else 10
                for (deg in (-leanScale.maxDegrees)..(leanScale.maxDegrees) step tickStep) {
                    val angle = zeroAngle + (deg * angleScale)
                    val rad = Math.toRadians(angle.toDouble())
                    val isZero = deg == 0
                    val isMajor = deg % 15 == 0 || deg == -leanScale.maxDegrees || deg == leanScale.maxDegrees
                    val tickLen = if (isZero) 16.dp.toPx() else if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                    val strokeW = if (isZero) 3.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.2.dp.toPx()
                    val tickColor = if (isZero) Color.White else if (isMajor) SubtleGray else SubtleGray.copy(alpha = 0.4f)

                    val start = Offset(
                        center.x + (radius - 12.dp.toPx() - tickLen) * cos(rad).toFloat(),
                        center.y + (radius - 12.dp.toPx() - tickLen) * sin(rad).toFloat()
                    )
                    val end = Offset(
                        center.x + (radius - 12.dp.toPx()) * cos(rad).toFloat(),
                        center.y + (radius - 12.dp.toPx()) * sin(rad).toFloat()
                    )
                    drawLine(color = tickColor, start = start, end = end, strokeWidth = strokeW, cap = StrokeCap.Round)
                }

                // Left Peak Ghost Marker
                if (maxLeft > 0f) {
                    val pAngle = zeroAngle - (maxLeft.coerceAtMost(maxLimit) * angleScale)
                    val pRad = Math.toRadians(pAngle.toDouble())
                    val pEnd = Offset(
                        center.x + (radius - 16.dp.toPx()) * cos(pRad).toFloat(),
                        center.y + (radius - 16.dp.toPx()) * sin(pRad).toFloat()
                    )
                    drawLine(color = secondaryColor, start = center, end = pEnd, strokeWidth = 2.5.dp.toPx())
                }

                // Right Peak Ghost Marker
                if (maxRight > 0f) {
                    val pAngle = zeroAngle + (maxRight.coerceAtMost(maxLimit) * angleScale)
                    val pRad = Math.toRadians(pAngle.toDouble())
                    val pEnd = Offset(
                        center.x + (radius - 16.dp.toPx()) * cos(pRad).toFloat(),
                        center.y + (radius - 16.dp.toPx()) * sin(pRad).toFloat()
                    )
                    drawLine(color = secondaryColor, start = center, end = pEnd, strokeWidth = 2.5.dp.toPx())
                }

                // Main Sweeping Analog Needle
                val needleAngle = zeroAngle + (animatedLean * angleScale)
                val needleRad = Math.toRadians(needleAngle.toDouble())
                val needleLen = radius - 18.dp.toPx()
                val needleEnd = Offset(
                    center.x + needleLen * cos(needleRad).toFloat(),
                    center.y + needleLen * sin(needleRad).toFloat()
                )
                val tailLen = 22.dp.toPx()
                val tailEnd = Offset(
                    center.x - tailLen * cos(needleRad).toFloat(),
                    center.y - tailLen * sin(needleRad).toFloat()
                )

                // Needle Drop Glow
                drawLine(
                    color = primaryColor.copy(alpha = 0.4f),
                    start = tailEnd,
                    end = needleEnd,
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Solid Needle
                drawLine(
                    color = primaryColor,
                    start = tailEnd,
                    end = needleEnd,
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Chrome Center Hub
                drawCircle(color = Color(0xFF12141A), radius = 18.dp.toPx(), center = center)
                drawCircle(color = Color(0xFF2C3242), radius = 18.dp.toPx(), center = center, style = Stroke(width = 1.5.dp.toPx()))
                drawCircle(color = primaryColor, radius = 7.dp.toPx(), center = center)
                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = center)
            }

            // Digital Inset Sub-Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 110.dp)
            ) {
                val leanAbs = abs(animatedLean).toInt()
                val dir = when {
                    animatedLean < -1.5f -> "◄ L"
                    animatedLean > 1.5f -> "R ►"
                    else -> "UP"
                }
                Text(
                    text = "$leanAbs°",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = dir,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
