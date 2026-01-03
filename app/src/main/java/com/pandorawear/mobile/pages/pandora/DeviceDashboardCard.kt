package com.pandorawear.mobile.pages.pandora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pandorawear.mobile.R
import com.pandorawear.mobile.models.AlarmDeviceUiModel
import kotlin.math.max

@Composable
fun DeviceDashboardCard(
    device: AlarmDeviceUiModel,
    lastSyncAtEpochMs: Long?,
    onEngineConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TopHeader(
            title = device.name,
            badgeText = if (device.isArmed) "ARMED" else "DISARMED",
        )

        HeroCard(
            isArmed = device.isArmed,
            isEngineOn = device.engineRpm > 0,
            lastSyncAtEpochMs = lastSyncAtEpochMs,
        )

        MetricsGrid(
            batteryVoltage = device.batteryVoltage,
            fuel = device.fuelTank,
            cabinTemp = device.cabinTemp,
            engineTemp = device.engineTemp,
        )

        QuickActionsRow(
            device = device,
            onEngineConfirmed = onEngineConfirmed,
        )
    }
}

@Composable
private fun TopHeader(
    title: String,
    badgeText: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    isArmed: Boolean,
    isEngineOn: Boolean,
    lastSyncAtEpochMs: Long?,
) {
    val cs = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large

    val base = cs.surfaceContainerHigh
    val blue = cs.primary.copy(alpha = 0.12f) // чуть ярче под эталон

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(shape)
                .background(base)
        ) {
            // right glow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(blue, Color.Transparent),
                            center = Offset(980f, 160f),
                            radius = 980f
                        )
                    )
            )

            Icon(
                painter = painterResource(R.drawable.freelander_vector),
                contentDescription = null,
                tint = cs.onSurface.copy(alpha = 0.20f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 50.dp, y = 28.dp)
                    .graphicsLayer(
                        scaleX = 1.35f,
                        scaleY = 1.35f
                    )
                    .width(340.dp)
                    .height(220.dp),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = when {
                        isEngineOn -> "Двигатель запущен"
                        isArmed -> "В охране"
                        else -> "Снята с охраны"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = buildString {
                        append("Онлайн")
                        formatRelativeTime(lastSyncAtEpochMs)?.let {
                            append(" • Обновлено ").append(it)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(999.dp),
                color = cs.primaryContainer,
            ) {
                Text(
                    text = if (isEngineOn) "ENGINE" else "READY",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    device: AlarmDeviceUiModel,
    onEngineConfirmed: () -> Unit,
) {
    val tileHeight = 50.dp
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EngineStartButton(
            isEngineOn = device.engineRpm > 0,
            onLongPressOverOneSecond = onEngineConfirmed,
            shapeVal = shape,
            modifier = Modifier
                .weight(1f)
                .height(tileHeight),
        )

        QuickActionStub(
            title = if (device.isArmed) "UNLOCK" else "LOCK",
            iconRes = Icons.Default.Lock,
            height = tileHeight,
            shapeVal = shape,
            modifier = Modifier.weight(1f),
        )

    }
}

@Composable
private fun QuickActionStub(
    title: String,
    iconRes: ImageVector,
    height: Dp,
    shapeVal: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {


    Surface(
        modifier = modifier.height(height),
        shape = shapeVal,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = iconRes,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MetricsGrid(
    batteryVoltage: Double,
    fuel: Int,
    cabinTemp: Double,
    engineTemp: Double,
) {
    val bigHeight = 110.dp
    val smallHeight = 92.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 🔹 ПЕРВЫЙ РЯД — БОЛЬШЕ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricCard(
                title = "АКБ",
                value = String.format("%.1fV", batteryVoltage),
                iconRes = R.drawable.battery_icon_512_vector,
                accent = MetricAccent.Blue,
                height = bigHeight,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Топливо",
                value = "$fuel%",
                iconRes = R.drawable.fuel_icon_512_vector,
                accent = MetricAccent.Blue,
                height = bigHeight,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                title = "Темп. салона",
                value = "$cabinTemp°",
                iconRes = R.drawable.cabin_temp_icon_512_vector,
                accent = MetricAccent.Warm,
                height = smallHeight,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Темп. двигателя",
                value = "$engineTemp°",
                iconRes = R.drawable.engine_temp_icon_512_vector,
                accent = MetricAccent.Warm,
                height = smallHeight,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * We use “glow recipes” rather than a single gradient fill.
 * This removes the “rectangle overlay” artifact and matches the reference look.
 */
private enum class MetricAccent { Blue, BlueSoft, Warm, None }

@Composable
private fun MetricCard(
    title: String,
    value: String,
    iconRes: Int,
    accent: MetricAccent,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large

    val base = cs.surfaceContainer

    // brighter glows (closer to reference)
    val blueStrong = cs.primary.copy(alpha = 0.34f)
    val blueMid = cs.primary.copy(alpha = 0.22f)
    val blueSoft = cs.primary.copy(alpha = 0.19f)

    // warm accent (subtle amber)
    val warm = Color(0xFFFFB45C).copy(alpha = 0.18f)

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        // IMPORTANT: NO padding on the layer that hosts the glows
        Box(
            modifier = Modifier
                .clip(shape)
                .background(base)
                .height(height)
        ) {
            // 1) global soft shading (full card area)
            Glow(
                color = cs.onSurface.copy(alpha = 0.05f),
                center = Offset(180f, -120f),
                radius = 760f
            )

            // 2) accent glows (full card area; centers outside bounds to avoid edges)
            when (accent) {
                MetricAccent.Blue -> {
                    Glow(color = blueStrong, center = Offset(980f, -260f), radius = 1180f)
                    Glow(color = blueMid, center = Offset(980f, 520f), radius = 980f)
                }

                MetricAccent.BlueSoft -> {
                    Glow(color = blueStrong, center = Offset(980f, -200f), radius = 890f)
                    Glow(color = blueMid, center = Offset(980f, 320f), radius = 730f)
                }

                MetricAccent.Warm -> {
                    Glow(color = warm, center = Offset(-240f, 520f), radius = 980f)
                    Glow(color = blueSoft, center = Offset(980f, -300f), radius = 1180f)
                }

                MetricAccent.None -> Unit
            }

            // 3) content padding goes here (separate layer)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                    )
                }

                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(70.dp),
                )
            }
        }
    }
}

@Composable
private fun Glow(
    color: Color,
    center: Offset,
    radius: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = center,
                    radius = radius
                )
            )
    )
}


private fun formatRelativeTime(lastSyncAtEpochMs: Long?): String? {
    if (lastSyncAtEpochMs == null) return null
    val now = System.currentTimeMillis()
    val diffMs = max(0L, now - lastSyncAtEpochMs)
    val sec = diffMs / 1000L
    return when {
        sec < 5 -> "только что"
        sec < 60 -> "$sec сек назад"
        sec < 3600 -> "${sec / 60} мин назад"
        else -> "${sec / 3600} ч назад"
    }
}
