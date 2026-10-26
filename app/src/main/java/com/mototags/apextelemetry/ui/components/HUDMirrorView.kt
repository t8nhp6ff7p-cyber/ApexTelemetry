package com.mototags.apextelemetry.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mototags.apextelemetry.model.SpeedUnit
import com.mototags.apextelemetry.ui.theme.DarkSurface
import com.mototags.apextelemetry.ui.theme.SubtleGray
import kotlin.math.abs

@Composable
fun HUDMirrorView(
    currentLean: Float,
    speedKmh: Float,
    maxLeft: Float,
    maxRight: Float,
    speedUnit: SpeedUnit,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    val displaySpeed = (speedKmh * speedUnit.factorToKmh).toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(scaleX = -1f) // Mirror horizontal for windshield reflection
            .background(DarkSurface, RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "WINDSHIELD REFLECTION HUD",
                style = MaterialTheme.typography.labelSmall,
                color = SubtleGray
            )

            // Giant Speed
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$displaySpeed",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 94.sp),
                    color = primaryColor,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = speedUnit.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Peak and live angles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "PEAK L", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                    Text(text = "${maxLeft.toInt()}°", style = MaterialTheme.typography.headlineMedium, color = secondaryColor, fontWeight = FontWeight.Black)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val leanAbs = abs(currentLean).toInt()
                    val dir = if (currentLean < -1.5f) "◄ L" else if (currentLean > 1.5f) "R ►" else "UP"
                    Text(
                        text = "$leanAbs° $dir",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(text = "ROLL ANGLE", style = MaterialTheme.typography.labelSmall, color = primaryColor, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "PEAK R", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                    Text(text = "${maxRight.toInt()}°", style = MaterialTheme.typography.headlineMedium, color = secondaryColor, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
