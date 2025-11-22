package com.pandorawear.wear.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import com.pandorawear.wear.R


@Composable
fun EngineStartButton(
    isEngineOn: Boolean,
    onLongPressOverOneSecond: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    var isPressed by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var flashTrigger by remember { mutableStateOf(0) }

    val buttonSize = 50.dp

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f,
        label = "engine_start_scale",
    )

    val flashProgress = remember { Animatable(0f) }

    LaunchedEffect(flashTrigger) {
        if (flashTrigger > 0) {
            flashProgress.snapTo(0f)
            flashProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250),
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            val start = System.currentTimeMillis()

            while (true) {
                val elapsed = System.currentTimeMillis() - start
                val fraction = (elapsed / 1000f).coerceIn(0f, 1f)
                progress = fraction

                if (!isPressed) {
                    progress = 0f
                    break
                }

                if (elapsed >= 2000L) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPressOverOneSecond()
                    flashTrigger++
                    isPressed = false
                    progress = 0f
                    break
                }

                kotlinx.coroutines.delay(16)
            }
        } else {
            progress = 0f
        }
    }

    Box(
        modifier = modifier.size(70.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (progress > 0f) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(70.dp),
                strokeWidth = 8.dp,
                colors = ProgressIndicatorDefaults.colors(
                    indicatorColor = if (isEngineOn) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                ),
            )
        }

        // 2) Поверх — флеш
        val flashAlpha = (1f - flashProgress.value) * 0.25f
        val flashScale = 1f + flashProgress.value * 1.2f

        if (flashAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .graphicsLayer {
                        alpha = flashAlpha
                        scaleX = flashScale
                        scaleY = flashScale
                    }
                    .clip(CircleShape)
                    .background(
                        if (isEngineOn)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    )
            )
        }

        // 3) И в самом верху — кнопка
        Box(
            modifier = Modifier
                .size(buttonSize)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                )
                .clip(CircleShape)
                .background(
                    if (isEngineOn)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            val released = tryAwaitRelease()
                            isPressed = false
                        }
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = if (isEngineOn)
                    painterResource(R.drawable.engine_stop_fan_512_vector)
                else
                    painterResource(R.drawable.engine_start_fan_512_vector),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(38.dp),
            )
        }
    }
}