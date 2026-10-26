package com.mototags.apextelemetry.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.ui.theme.ArcTrackColor
import com.mototags.apextelemetry.ui.theme.DarkCard
import com.mototags.apextelemetry.ui.theme.NeonGreen
import com.mototags.apextelemetry.ui.theme.NeonOrange
import com.mototags.apextelemetry.ui.theme.SubtleGray
import java.util.Locale

@Composable
fun GForceMeter(
    lateralG: Float,
    longitudinalG: Float,
    maxLateralG: Float,
    maxBrakeG: Float,
    modifier: Modifier = Modifier
) {
    val animLatG by animateFloatAsState(
        targetValue = lateralG.coerceIn(-1.5f, 1.5f),
        animationSpec = spring(stiffness = 700f),
        label = "LatGAnimation"
    )
    val animLongG by animateFloatAsState(
        targetValue = longitudinalG.coerceIn(-1.5f, 1.5f),
        animationSpec = spring(stiffness = 700f),
        label = "LongGAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "G-FORCE RADAR",
            style = MaterialTheme.typography.labelSmall,
            color = SubtleGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            Canvas(modifier = Modifier.size(150.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.width / 2f

                // Target scales: 0.5G and 1.0G (and 1.5G boundary)
                val r05 = maxRadius * (0.5f / 1.5f)
                val r10 = maxRadius * (1.0f / 1.5f)

                // Background rings
                drawCircle(color = DarkCard, radius = maxRadius)
                drawCircle(color = ArcTrackColor, radius = r05, style = Stroke(width = 1.dp.toPx()))
                drawCircle(color = ArcTrackColor, radius = r10, style = Stroke(width = 1.5.dp.toPx()))
                drawCircle(color = SubtleGray.copy(alpha = 0.4f), radius = maxRadius, style = Stroke(width = 1.5.dp.toPx()))

                // Crosshairs
                drawLine(
                    color = ArcTrackColor,
                    start = Offset(0f, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = ArcTrackColor,
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height),
                    strokeWidth = 1.5.dp.toPx()
                )

                // G-Ball Position
                // Screen X: lateralG (+ is right, - is left)
                // Screen Y: longitudinalG (+ is accel/up, - is brake/down) -> in canvas Y down is positive, so invert
                val gScale = maxRadius / 1.5f
                val ballX = (center.x + animLatG * gScale).coerceIn(10f, size.width - 10f)
                val ballY = (center.y - animLongG * gScale).coerceIn(10f, size.height - 10f)

                // Glow ring around G-Ball
                drawCircle(
                    color = NeonGreen.copy(alpha = 0.3f),
                    radius = 14.dp.toPx(),
                    center = Offset(ballX, ballY)
                )
                // G-Ball solid
                drawCircle(
                    color = NeonGreen,
                    radius = 7.dp.toPx(),
                    center = Offset(ballX, ballY)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = String.format(Locale.US, "LAT %.2fg | BRK %.2fg", maxLateralG, maxBrakeG),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = NeonOrange,
            fontWeight = FontWeight.Bold
        )
    }
}
