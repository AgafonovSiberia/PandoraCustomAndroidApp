package com.pandorawear.wear.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Displays battery voltage and engine temperature as curved gauge arcs
 * with bold numeric values at the start, from which the arcs grow
 */
@Composable
fun DualMetricGaugeLines(
    batteryVoltage: Double?,
    engineTemp: Double?,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme

    // Normalize values to 0..1 range
    val battProgress = if (batteryVoltage == null) 0f else normalize(batteryVoltage, 11.7, 14.2).toFloat()
    val tempProgress = if (engineTemp == null) 0f else normalize(engineTemp, -17.0, 70.0).toFloat()

    // Color scheme inspired by Freelander reference
    // Battery - blue tones (like АКБ card)
    val batteryTrackStart = Color(0xFF1A2C3D) // Dark blue
    val batteryTrackEnd = Color(0xFF2A3D52) // Slightly lighter blue
    
    val batteryProgressStart = Color(0xFF4A9EFF) // Bright blue
    val batteryProgressMid = Color(0xFF3B7FCC) // Medium blue
    val batteryProgressEnd = Color(0xFF2D6099) // Deep blue
    
    // Engine temp - blue-cyan tones (like Темп. двигателя card)
    val tempTrackStart = Color(0xFF1A2C3D) // Dark blue
    val tempTrackEnd = Color(0xFF253847) // Blue-gray
    
    val tempProgressStart = Color(0xFF5DADE2) // Light cyan-blue
    val tempProgressMid = Color(0xFF3498DB) // Azure blue
    val tempProgressEnd = Color(0xFF2874A6) // Deep blue
    
    // Text gradient - pure white to light gray
    val textGradientStart = Color(0xFFFFFFFF) // Pure white
    val textGradientEnd = Color(0xFFE0E0E0) // Light gray (whiter than before)

    Box(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            val d = min(w, h)
            val inset = d * 0.03f
            val radius = (d - inset * 2) / 2f
            val centerX = w / 2f
            val centerY = h / 2f

            // Arc settings
            // Left arc (Battery): starts at 155°, sweeps 70° to 225°
            val leftStartAngle = 155f
            val leftSweep = 70f
            
            // Right arc (Temperature): starts at 335°, sweeps 70° to 45°
            val rightStartAngle = 335f
            val rightSweep = 70f

            // Position for battery label (left side, before arc)
            val battLabelAngle = Math.toRadians(145.0) // 10° before arc starts
            val battLabelRadius = radius - 10f
            val battLabelX = centerX + battLabelRadius * cos(battLabelAngle).toFloat()
            val battLabelY = centerY + battLabelRadius * sin(battLabelAngle).toFloat()

            // Position for temperature label (right side, before arc)
            val tempLabelAngle = Math.toRadians(325.0) // 10° before arc starts
            val tempLabelRadius = radius + 5f // Closer to screen edge
            val tempLabelX = centerX + tempLabelRadius * cos(tempLabelAngle).toFloat()
            val tempLabelY = centerY + tempLabelRadius * sin(tempLabelAngle).toFloat()

            // Draw numeric labels (FIRST, so arcs appear to grow from them)
            // Battery label - rotation follows screen curvature at 145°
            val battRotation = (battLabelAngle * 180 / Math.PI).toFloat() - 90f // Tangent to circle
            
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (battLabelX - 35f).toDp() },
                        y = with(density) { (battLabelY - 19f).toDp() }
                    )
                    .graphicsLayer {
                        rotationZ = battRotation
                    }
            ) {
                Text(
                    text = batteryVoltage?.let { String.format("%.1fВ", it) } ?: "--",
                    style = MaterialTheme.typography.displaySmall.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(textGradientStart, textGradientEnd),
                            start = Offset.Zero,
                            end = Offset(50f, 40f)
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            // Temperature label - rotation follows screen curvature at 325°
            // Add 180° for angles > 180° to keep text readable (not upside-down)
            val tempRotation = (tempLabelAngle * 180 / Math.PI).toFloat() - 90f + 180f // Tangent + flip correction
            
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (tempLabelX - 13f).toDp() },
                        y = with(density) { (tempLabelY - 11f).toDp() }
                    )
                    .graphicsLayer {
                        rotationZ = tempRotation
                    }
            ) {
                Text(
                    text = engineTemp?.let { String.format("%.0f°", it) } ?: "--",
                    style = MaterialTheme.typography.displaySmall.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(textGradientStart, textGradientEnd),
                            start = Offset.Zero,
                            end = Offset(40f, 140f)
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            // Draw arcs (SECOND, so they appear to grow from the numbers)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val arcSize = Size(d - inset * 2, d - inset * 2)
                val topLeft = Offset((size.width - arcSize.width) / 2f, (size.height - arcSize.height) / 2f)
                val thickness = 4.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)

                // Battery arc gradients (blue tones)
                val batteryTrackBrush = Brush.sweepGradient(
                    colors = listOf(batteryTrackStart, batteryTrackEnd, batteryTrackStart),
                    center = center
                )
                
                val batteryProgressBrush = Brush.sweepGradient(
                    colors = listOf(batteryProgressStart, batteryProgressMid, batteryProgressEnd, batteryProgressStart),
                    center = center
                )
                
                // Temperature arc gradients (cyan-blue tones)
                val tempTrackBrush = Brush.sweepGradient(
                    colors = listOf(tempTrackStart, tempTrackEnd, tempTrackStart),
                    center = center
                )
                
                val tempProgressBrush = Brush.sweepGradient(
                    colors = listOf(tempProgressStart, tempProgressMid, tempProgressEnd, tempProgressStart),
                    center = center
                )

                // Left arc (Battery) - track
                drawArc(
                    brush = batteryTrackBrush,
                    startAngle = leftStartAngle,
                    sweepAngle = leftSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = thickness, cap = StrokeCap.Round)
                )

                // Left arc (Battery) - progress
                if (battProgress > 0.01f) {
                    val progressSweep = leftSweep * battProgress
                    if (progressSweep >= 6f) {
                        drawArc(
                            brush = batteryProgressBrush,
                            startAngle = leftStartAngle,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = thickness, cap = StrokeCap.Round)
                        )
                    }
                }

                // Right arc (Temperature) - track
                drawArc(
                    brush = tempTrackBrush,
                    startAngle = rightStartAngle,
                    sweepAngle = rightSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = thickness, cap = StrokeCap.Round)
                )

                // Right arc (Temperature) - progress
                if (tempProgress > 0.01f) {
                    val progressSweep = rightSweep * tempProgress
                    if (progressSweep >= 6f) {
                        drawArc(
                            brush = tempProgressBrush,
                            startAngle = rightStartAngle,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = thickness, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}

private fun normalize(value: Double, min: Double, max: Double): Double {
    if (max <= min) return 0.0
    return ((value - min) / (max - min)).coerceIn(0.0, 1.0)
}
