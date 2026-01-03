package com.pandorawear.mobile.pages.pandora

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pandorawear.mobile.R
import com.pandorawear.mobile.models.AlarmDeviceUiModel
import kotlin.math.max

/**
 * Reference Dashboard (Variant 1)
 *
 * Constraints:
 * - Guard / trunk: disabled placeholders
 * - Engine: one dynamic button (Start/Stop), confirmation via 2s hold (EngineStartButton)
 */
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

        QuickActions(
            isEngineOn = device.engineRpm > 0,
            onEngineConfirmed = onEngineConfirmed,
        )

        Spacer(modifier = Modifier.height(6.dp))

//        FooterHint()
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

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
                    painter = painterResource(R.drawable.fuel_icon_512_vector),
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
    val shape = MaterialTheme.shapes.extraLarge

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.colorScheme.surfaceContainer,
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Ключевой якорь эталона — силуэт авто справа
            Icon(
                painter = painterResource(R.drawable.freelander_vector),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(210.dp)
                    .height(170.dp),
            )

            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = when {
                        isEngineOn -> "Двигатель запущен"
                        isArmed -> "В охране"
                        else -> "Снята с охраны"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                modifier = Modifier.align(Alignment.TopEnd),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = if (isEngineOn) "ENGINE" else "READY",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}


@Composable
private fun QuickActions(
    isEngineOn: Boolean,
    onEngineConfirmed: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Primary: engine (dynamic, hold-to-confirm)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EngineStartButton(
                isEngineOn = isEngineOn,
                onLongPressOverOneSecond = onEngineConfirmed,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = if (isEngineOn) "Стоп" else "Запуск",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Disabled placeholders
        QuickActionStub(
            title = "Снять",
            iconRes = R.drawable.cabin_temp_icon_512_vector,
            modifier = Modifier.weight(1f),
        )
        QuickActionStub(
            title = "Багажник",
            iconRes = R.drawable.cabin_temp_icon_512_vector,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickActionStub(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Топливо",
                value = "$fuel",
                iconRes = R.drawable.fuel_icon_512_vector,
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
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Темп. двигателя",
                value = "$engineTemp°",
                iconRes = R.drawable.engine_temp_icon_512_vector,
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp),
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

//@Composable
//private fun FooterHint() {
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shape = MaterialTheme.shapes.large,
//        color = MaterialTheme.colorScheme.surfaceContainer,
//    ) {
//        Text(
//            text = "Удерживайте кнопку запуска 2 секунды для подтверждения",
//            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//        )
//    }
//}

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
