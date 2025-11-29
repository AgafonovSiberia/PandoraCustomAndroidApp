package com.pandorawear.wear.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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

    val buttonWidth = 140.dp
    val buttonHeight = 60.dp

    val longPressDurationMs = 2000

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.05f else 1f,
        label = "engine_start_scale",
    )

    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = if (isPressed) {
            tween(
                durationMillis = longPressDurationMs,
                easing = LinearEasing,
            )
        } else {
            tween(
                durationMillis = 150,
                easing = FastOutSlowInEasing,
            )
        },
        label = "engine_press_progress",
    )

    // анимация вспышки
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

    val shape = RoundedCornerShape(20.dp)

    val baseColor =
        if (isEngineOn) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.primaryContainer

    val progressColor =
        if (isEngineOn) MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)

    val flashAlpha = (1f - flashProgress.value) * 0.25f
    val flashScale = 1f + flashProgress.value * 0.2f

    Box(
        modifier = modifier
            .width(buttonWidth)
            .height(buttonHeight),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                )
                .clip(shape)
                .background(baseColor)
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
            if (pressProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(pressProgress)
                        .align(Alignment.CenterStart)
                        .background(progressColor),
                )
            }

            Icon(
                painter = if (isEngineOn)
                    painterResource(R.drawable.engine_stop_fan_512_vector)
                else
                    painterResource(R.drawable.engine_start_fan_512_vector),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp),
            )
        }


        if (flashAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        alpha = flashAlpha
                        scaleX = flashScale
                        scaleY = flashScale
                    }
                    .clip(shape)
                    .background(
                        if (isEngineOn)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    ),
            )
        }
    }
}