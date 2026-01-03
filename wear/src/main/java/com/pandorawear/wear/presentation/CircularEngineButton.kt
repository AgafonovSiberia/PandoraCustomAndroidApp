package com.pandorawear.wear.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.pandorawear.wear.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Circular engine start/stop button for Wear OS
 * Requires a long press (2 seconds) to activate
 */
@Composable
fun CircularEngineButton(
    isEngineOn: Boolean,
    onLongPressOverOneSecond: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    var hasTriggered by remember { mutableStateOf(false) }
    var flashToken by remember { mutableStateOf(0) }

    val buttonSize = 100.dp
    val longPressDurationMs = 2000

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "circular_engine_scale",
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
        label = "circular_engine_progress",
    )

    val flashProgress = remember { Animatable(1f) }
    LaunchedEffect(flashToken) {
        if (flashToken > 0) {
            flashProgress.snapTo(0f)
            flashProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300),
            )
        }
    }

    // Gradient colors inspired by Freelander - aggressive, darker tones
    val buttonGradient = if (isEngineOn) {
        // Stop button - aggressive red gradient
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFFF4444), // Bright saturated red center
                Color(0xFFCC0000), // Deep red mid
                Color(0xFF8B0000), // Very dark red edge
            ),
            center = Offset.Unspecified,
            radius = Float.POSITIVE_INFINITY
        )
    } else {
        // Start button - aggressive blue gradient with darker tones
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF2E86C1), // Rich medium blue center
                Color(0xFF1B4F72), // Deep blue mid
                Color(0xFF0D2A42), // Very dark blue edge
            ),
            center = Offset.Unspecified,
            radius = Float.POSITIVE_INFINITY
        )
    }

    val progressGradient = if (isEngineOn) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFFF6B6B).copy(alpha = 0.8f),
                Color(0xFFCC0000).copy(alpha = 0.6f),
            ),
            center = Offset.Unspecified,
            radius = Float.POSITIVE_INFINITY
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF3498DB).copy(alpha = 0.8f),
                Color(0xFF1B4F72).copy(alpha = 0.6f),
            ),
            center = Offset.Unspecified,
            radius = Float.POSITIVE_INFINITY
        )
    }

    val flashAlpha = (1f - flashProgress.value) * 0.35f
    val flashScale = 1f + flashProgress.value * 0.15f

    Box(
        modifier = modifier.size(buttonSize),
        contentAlignment = Alignment.Center,
    ) {
        // Main button
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                )
                .clip(CircleShape)
                .background(buttonGradient)
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
            // Progress indicator (radial fill)
            if (pressProgress > 0f) {
                Box(
                    modifier = Modifier
                        .size(buttonSize * pressProgress)
                        .clip(CircleShape)
                        .background(progressGradient),
                )
            }

            // Icon and label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 8.dp) // Shift content up
            ) {
                Icon(
                    painter = if (isEngineOn)
                        painterResource(R.drawable.engine_stop_fan_512_vector)
                    else
                        painterResource(R.drawable.engine_start_fan_512_vector),
                    contentDescription = if (isEngineOn) "Остановить двигатель" else "Запустить двигатель",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(42.dp),
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = if (isEngineOn) "STOP" else "START",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
            }
        }

        // Flash overlay
        if (flashAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
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
                    ),
            )
        }
    }
}
