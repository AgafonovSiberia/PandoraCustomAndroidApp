package com.pandorawear.wear.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.pandorawear.wear.R
import kotlin.math.min

/**
 * Bezel-anchored arcs:
 * - arcs are pinned to the screen edge (safe inset), independent of the center button.
 * - battery (left/top arc), engine temp (right/top arc)
 */
@Composable
fun DualMetricRing(
    batteryVoltage: Double?,
    engineTemp: Double?,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme

    val battProgress = if (batteryVoltage == null) 0f else normalize(batteryVoltage, 11.8, 12.8).toFloat()
    val tempProgress = if (engineTemp == null) 0f else normalize(engineTemp, 40.0, 110.0).toFloat()
    val tempHot = (engineTemp ?: 0.0) >= 90.0

    val track = cs.onSurface.copy(alpha = 0.10f)
    val battColor = cs.primary.copy(alpha = 0.95f)
    val tempColor = (if (tempHot) cs.error else Color(0xFFFFB45C)).copy(alpha = 0.95f)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val d = min(w, h)

            // === Bezel anchored geometry ===
            // Safe inset from the physical edge (keeps arcs off the bezel/rounding)
            val inset = d * 0.08f
            val thickness = d * 0.045f

            val arcSize = Size(d - inset * 2, d - inset * 2)
            val topLeft = Offset((w - arcSize.width) / 2f, (h - arcSize.height) / 2f)

            // Two top arcs with a gap at 12 o'clock (keeps it airy)
            // Left/top arc: ~ 200° -> 310°
            val leftStart = 200f
            val sweepTotal = 110f

            // Right/top arc: ~ -20° -> 90° (same sweep)
            val rightStart = -20f
            val rightSweepTotal = 110f

            // Track
            drawArc(
                color = track,
                startAngle = leftStart,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = thickness, cap = StrokeCap.Round)
            )
            drawArc(
                color = track,
                startAngle = rightStart,
                sweepAngle = rightSweepTotal,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = thickness, cap = StrokeCap.Round)
            )

            // Progress (avoid "dot" on tiny values)
            drawProgressArc(
                color = battColor,
                startAngle = leftStart,
                sweepTotal = sweepTotal,
                progress = battProgress,
                topLeft = topLeft,
                arcSize = arcSize,
                thickness = thickness
            )
            drawProgressArc(
                color = tempColor,
                startAngle = rightStart,
                sweepTotal = rightSweepTotal,
                progress = tempProgress,
                topLeft = topLeft,
                arcSize = arcSize,
                thickness = thickness
            )
        }

        // Labels stay inside the top area (do not touch bezel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, start = 18.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetricLabel(
                iconRes = R.drawable.battery_icon_512_vector,
                text = batteryVoltage?.let { String.format("%.1fV", it) } ?: "--",
                tint = cs.onSurface.copy(alpha = 0.86f),
            )

            Spacer(Modifier.weight(1f))

            MetricLabel(
                iconRes = R.drawable.engine_temp_icon_512_vector,
                text = engineTemp?.let { String.format("%.0f°", it) } ?: "--",
                tint = (if (tempHot) cs.error else cs.onSurface).copy(alpha = 0.88f),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawProgressArc(
    color: Color,
    startAngle: Float,
    sweepTotal: Float,
    progress: Float,
    topLeft: Offset,
    arcSize: Size,
    thickness: Float,
) {
    val sweep = sweepTotal * progress.coerceIn(0f, 1f)
    if (sweep < 6f) return // prevents the small "blob" artifact
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = thickness, cap = StrokeCap.Round)
    )
}

@Composable
private fun MetricLabel(
    iconRes: Int,
    text: String,
    tint: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun normalize(value: Double, min: Double, max: Double): Double {
    if (max <= min) return 0.0
    return ((value - min) / (max - min)).coerceIn(0.0, 1.0)
}
