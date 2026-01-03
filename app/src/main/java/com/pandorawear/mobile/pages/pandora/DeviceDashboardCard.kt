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

        Spacer(modifier = Modifier.height(4.dp))
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
                // Если lock-иконки нет физически — оставляем как есть (ты мог удалить).
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
    val cs = MaterialTheme.colorScheme

    // Градиент “как в эталоне”: тёмная база + уход в голубой оттенок справа/сверху
    val heroBrush = Brush.linearGradient(
        listOf(
            cs.surfaceContainerHigh,
            cs.surfaceContainer,
            cs.primary.copy(alpha = 0.12f),
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(shape)
                .background(heroBrush)
                .padding(16.dp)
        ) {
            // Силуэт авто — крупнее и ближе к эталону
            Icon(
                painter = painterResource(R.drawable.freelander_vector),
                contentDescription = null,
                tint = cs.onSurface.copy(alpha = 0.14f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(280.dp)
                    .height(190.dp),
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
                )
            }

            Surface(
                modifier = Modifier.align(Alignment.TopEnd),
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
private fun QuickActions(
    isEngineOn: Boolean,
    onEngineConfirmed: () -> Unit,
) {
    // Делаем quick actions ниже: 76dp + убираем подписи снизу (как в эталоне)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Engine — шире (2x), чтобы текст не ломался и выглядел как primary action
        EngineStartButton(
            isEngineOn = isEngineOn,
            onLongPressOverOneSecond = onEngineConfirmed,
            modifier = Modifier.weight(2f),
        )

        QuickActionStub(
            title = "Снять",
            iconRes = R.drawable.cabin_temp_icon_512_vector, // заглушка — ресурс можно заменить
            modifier = Modifier.weight(1f),
        )

        QuickActionStub(
            title = "Багажник",
            iconRes = R.drawable.cabin_temp_icon_512_vector, // заглушка — ресурс можно заменить
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
    Surface(
        modifier = modifier.height(76.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
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

    // Лёгкий “голубой” градиент на акцентных карточках, как в эталоне
    val bgBrush = if (accent) {
        Brush.linearGradient(
            listOf(
                cs.surfaceContainer,
                cs.surfaceContainerHigh,
                cs.primary.copy(alpha = 0.10f),
            )
        )
    } else {
        Brush.linearGradient(listOf(cs.surfaceContainer, cs.surfaceContainer))
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bgBrush)
                .padding(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurfaceVariant,
                    )

                    // Иконка: фон светлее + иконка крупнее (это твой пункт №2)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cs.surfaceContainerHighest,
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(22.dp),
                        )
                    }
                }

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onSurface,
                    fontWeight = FontWeight.SemiBold,
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
