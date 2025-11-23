package com.pandorawear.wear.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import com.pandorawear.wear.R
import androidx.compose.runtime.*

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun EngineStartButton(
    isEngineOn: Boolean,
    onLongPressOverOneSecond: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    var hasTriggered by remember { mutableStateOf(false) }
    var flashToken by remember { mutableStateOf(0) }

    val progressSize = 70.dp
    val buttonSize = 50.dp

    val longPressDurationMs = 2000

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f,
        label = "engine_start_scale",
    )


    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isPressed) longPressDurationMs else 150,
            easing = FastOutSlowInEasing,
        ),
        label = "engine_press_progress",
    )

    val flashProgress = remember { Animatable(1f) }
    LaunchedEffect(flashToken) {
        if (flashToken > 0) {
            flashProgress.snapTo(0f)
            flashProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250),
            )
        }
    }

    Box(
        modifier = modifier.size(progressSize),
        contentAlignment = Alignment.Center,
    ) {
        // 1) кольцо прогресса вокруг кнопки
        if (pressProgress > 0f) {
            CircularProgressIndicator(
                progress = { pressProgress },
                modifier = Modifier.size(progressSize),
                strokeWidth = 6.dp,
                allowProgressOverflow = true,
                colors = ProgressIndicatorDefaults.colors(
                    indicatorColor = if (isEngineOn) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                ),
            )
        }

        // 2) флеш над кольцом
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
                            hasTriggered = false
                            isPressed = true

                            val longPressJob = scope.launch {
                                delay(longPressDurationMs.toLong())
                                if (isPressed && !hasTriggered) {
                                    hasTriggered = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongPressOverOneSecond()
                                    flashToken++

                                    isPressed = false


                                }
                            }

                            try {
                                tryAwaitRelease()
                            } finally {
                                isPressed = false
                                longPressJob.cancel()
                            }
                        },
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