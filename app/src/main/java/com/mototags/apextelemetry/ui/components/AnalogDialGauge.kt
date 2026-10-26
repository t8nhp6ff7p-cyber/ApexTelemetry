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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.ui.theme.ArcTrackColor
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.DarkSurface
import com.mototags.apextelemetry.ui.theme.NeonOrange
import com.mototags.apextelemetry.ui.theme.NeonRed
import com.mototags.apextelemetry.ui.theme.SubtleGray
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogDialGauge(
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
        label = "AnalogNeedle"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Peak Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "PEAK L", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                Text(text = "${maxLeft.toInt()}°", style = MaterialTheme.typography.titleLarge, color = secondaryColor, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "PEAK R", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                Text(text = "${maxRight.toInt()}°", style = MaterialTheme.typography.titleLarge, color = secondaryColor, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Circular Analog Gauge Face
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(270.dp)
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 10.dp.toPx()

                // Dial Face Background & Outer Rim
                drawCircle(color = DarkSurface, radius = radius, center = center)
                drawCircle(color = DarkCard, radius = radius, center = center, style = Stroke(width = 4.dp.toPx()))
                drawCircle(color = ArcTrackColor, radius = radius - 8.dp.toPx(), center = center, style = Stroke(width = 1.5.dp.toPx()))

                // Top Upright angle is 270 degrees in Android Canvas
                val zeroAngle = 270f

                // Dial Ticks: -60 to +60
                for (deg in -60..60 step 5) {
                    val angle = zeroAngle + deg
                    val rad = Math.toRadians(angle.toDouble())
                    val isMajor = deg % 15 == 0
                    val isZero = deg == 0
                    val tickLen = if (isZero) 18.dp.toPx() else if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                    val strokeW = if (isZero) 3.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                    val tickColor = if (isZero) Color.White else if (isMajor) SubtleGray else SubtleGray.copy(alpha = 0.5f)

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

                // Left Peak Ghost Needle
                if (maxLeft > 0f) {
                    val pAngle = zeroAngle - maxLeft.coerceAtMost(60f)
                    val pRad = Math.toRadians(pAngle.toDouble())
                    val pEnd = Offset(
                        center.x + (radius - 16.dp.toPx()) * cos(pRad).toFloat(),
                        center.y + (radius - 16.dp.toPx()) * sin(pRad).toFloat()
                    )
                    drawLine(color = secondaryColor.copy(alpha = 0.8f), start = center, end = pEnd, strokeWidth = 2.dp.toPx())
                }

                // Right Peak Ghost Needle
                if (maxRight > 0f) {
                    val pAngle = zeroAngle + maxRight.coerceAtMost(60f)
                    val pRad = Math.toRadians(pAngle.toDouble())
                    val pEnd = Offset(
                        center.x + (radius - 16.dp.toPx()) * cos(pRad).toFloat(),
                        center.y + (radius - 16.dp.toPx()) * sin(pRad).toFloat()
                    )
                    drawLine(color = secondaryColor.copy(alpha = 0.8f), start = center, end = pEnd, strokeWidth = 2.dp.toPx())
                }

                // Main Sweeping Analog Needle
                val needleAngle = zeroAngle + animatedLean
                val needleRad = Math.toRadians(needleAngle.toDouble())
                val needleLen = radius - 18.dp.toPx()
                val needleEnd = Offset(
                    center.x + needleLen * cos(needleRad).toFloat(),
                    center.y + needleLen * sin(needleRad).toFloat()
                )

                // Counterweight tail
                val tailLen = 24.dp.toPx()
                val tailEnd = Offset(
                    center.x - tailLen * cos(needleRad).toFloat(),
                    center.y - tailLen * sin(needleRad).toFloat()
                )

                // Draw Needle
                drawLine(
                    color = primaryColor,
                    start = tailEnd,
                    end = needleEnd,
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center Cap
                drawCircle(color = DarkCard, radius = 16.dp.toPx(), center = center)
                drawCircle(color = primaryColor, radius = 8.dp.toPx(), center = center)
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = center)
            }

            // Central Digital Sub-Display (Speedometer 55 style)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 100.dp)
            ) {
                val leanAbs = abs(animatedLean).toInt()
                val dir = when {
                    animatedLean < -1.5f -> "◄ L"
                    animatedLean > 1.5f -> "R ►"
                    else -> "UP"
                }
                Text(
                    text = "$leanAbs°",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 38.sp),
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
