package com.mototags.apextelemetry.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.ui.theme.ArcTrackColor
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.DarkSurface
import com.mototags.apextelemetry.ui.theme.SubtleGray
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RallyTwinGauge(
    currentLean: Float,
    maxLeft: Float,
    maxRight: Float,
    speedKmh: Float,
    maxSpeedKmh: Float,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    val animatedLean by animateFloatAsState(
        targetValue = currentLean.coerceIn(-65f, 65f),
        animationSpec = spring(stiffness = 800f),
        label = "RallyLean"
    )
    val animatedSpeed by animateFloatAsState(
        targetValue = speedKmh.coerceIn(0f, 220f),
        animationSpec = spring(stiffness = 700f),
        label = "RallySpeed"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Instrument Pod: Lean Angle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "ROLL ANGLE", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
            Spacer(modifier = Modifier.height(4.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 4.dp.toPx()

                    drawCircle(color = DarkCard, radius = radius, center = center)
                    drawCircle(color = ArcTrackColor, radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))

                    val zeroAngle = 270f
                    val needleAngle = zeroAngle + animatedLean
                    val needleRad = Math.toRadians(needleAngle.toDouble())
                    val end = Offset(
                        center.x + (radius - 10.dp.toPx()) * cos(needleRad).toFloat(),
                        center.y + (radius - 10.dp.toPx()) * sin(needleRad).toFloat()
                    )

                    // Draw tick at 0 (straight up)
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y - radius + 8.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )

                    drawLine(
                        color = primaryColor,
                        start = center,
                        end = end,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = center)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp)) {
                    Text(text = "${abs(animatedLean).toInt()}°", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black)
                    Text(
                        text = if (animatedLean < -1.5f) "LEFT" else if (animatedLean > 1.5f) "RIGHT" else "UPRIGHT",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "L ${maxLeft.toInt()}° | R ${maxRight.toInt()}°",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = secondaryColor,
                fontWeight = FontWeight.Bold
            )
        }

        // Right Instrument Pod: Speedometer
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "SPEED", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
            Spacer(modifier = Modifier.height(4.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 4.dp.toPx()

                    drawCircle(color = DarkCard, radius = radius, center = center)
                    drawCircle(color = ArcTrackColor, radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))

                    // Speed sweep from 140° to 400° (260 degree arc)
                    val startAngle = 140f
                    val sweep = (animatedSpeed / 200f) * 260f
                    val speedRad = Math.toRadians((startAngle + sweep).toDouble())
                    val end = Offset(
                        center.x + (radius - 10.dp.toPx()) * cos(speedRad).toFloat(),
                        center.y + (radius - 10.dp.toPx()) * sin(speedRad).toFloat()
                    )

                    drawLine(
                        color = secondaryColor,
                        start = center,
                        end = end,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = secondaryColor, radius = 5.dp.toPx(), center = center)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 36.dp)) {
                    Text(text = "${animatedSpeed.toInt()}", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
                    Text(text = "KM/H", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SubtleGray, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = "TOP: ${maxSpeedKmh.toInt()} km/h",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = secondaryColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
