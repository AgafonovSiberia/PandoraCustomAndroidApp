package com.pandorawear.mobile.pages.pandora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

        QuickActionsRow(
            isEngineOn = device.engineRpm > 0,
            onEngineConfirmed = onEngineConfirmed,
        )

        MetricsGrid(
            batteryVoltage = device.batteryVoltage,
            fuel = device.fuelTank,
            cabinTemp = device.cabinTemp,
            engineTemp = device.engineTemp,
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
                    painter = painterResource(R.drawable.cabin_temp_icon_512_vector),
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
    val shape = MaterialTheme.shapes.extraLarge

    // Ближе к эталону: тёмная база + “свет” справа в голубой
    val base = cs.surfaceContainerHigh
    val blue = cs.primary.copy(alpha = 0.22f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(shape)
                .background(base)
        ) {
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
                tint = cs.onSurface.copy(alpha = 0.16f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 24.dp, y = 12.dp)
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
    isEngineOn: Boolean,
    onEngineConfirmed: () -> Unit,
) {
    val tileHeight = 72.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EngineStartButton(
            isEngineOn = isEngineOn,
            onLongPressOverOneSecond = onEngineConfirmed,
            modifier = Modifier
                .weight(1f)
                .height(tileHeight),
        )

        QuickActionStub(
            title = "Снять",
            iconRes = R.drawable.cabin_temp_icon_512_vector,
            height = tileHeight,
            modifier = Modifier.weight(1f),
        )

        QuickActionStub(
            title = "Багажник",
            iconRes = R.drawable.cabin_temp_icon_512_vector,
            height = tileHeight,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickActionStub(
    title: String,
    iconRes: Int,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    // МЕНЬШЕ СКРУГЛЕНИЕ: убираем “пузырь”
    val shape = RoundedCornerShape(18.dp)

    Surface(
        modifier = modifier.height(height),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter = painterResource(iconRes),
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                title = "АКБ",
                value = String.format("%.1fV", batteryVoltage),
                iconRes = R.drawable.battery_icon_512_vector,
                accent = true,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Топливо",
                value = "$fuel",
                iconRes = R.drawable.fuel_icon_512_vector,
                accent = false,
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
                accent = false,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Темп. двигателя",
                value = "$engineTemp°",
                iconRes = R.drawable.engine_temp_icon_512_vector,
                accent = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    iconRes: Int,
    accent: Boolean,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.extraLarge

    // Убираем “прямоугольники”:
    // 1) мягкий общий градиент ВСЕМ карточкам (почти незаметный)
    // 2) акцент — дополнительный мягкий подсвет, центр вынесен за границы, радиус большой
    val base = cs.surfaceContainer
    val softTop = cs.onSurface.copy(alpha = 0.035f)
    val blue = cs.primary.copy(alpha = if (accent) 0.14f else 0.08f)

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(base)
                .height(92.dp)
                .padding(14.dp),
        ) {
            // общий “воздух” (не должен читаться как отдельный прямоугольник)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(softTop, Color.Transparent),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 500f),
                        )
                    )
            )

            // голубой подсвет справа сверху, но без жёстких краёв
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(blue, Color.Transparent),
                            center = Offset(850f, -120f),
                            radius = 900f
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }

                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
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
