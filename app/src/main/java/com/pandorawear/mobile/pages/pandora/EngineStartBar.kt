package com.pandorawear.mobile.pages.pandora

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pandorawear.mobile.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EngineStartBar(
    isEngineOn: Boolean,
    onLongPressOverOneSecond: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EngineStartButton(
            isEngineOn = isEngineOn,
            onLongPressOverOneSecond = onLongPressOverOneSecond,
        )
    }
}

/**
 * Primary engine control.
 * - One button (dynamic Start/Stop)
 * - Confirmation via hold (2 seconds)
 * - Progress fill inside button (left -> right)
 */
@Composable
fun EngineStartButton(
    isEngineOn: Boolean,
    onLongPressOverOneSecond: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    var isPressed by remember { mutableStateOf(false) }
    var hasTriggered by remember { mutableStateOf(false) }
    var flashToken by remember { mutableIntStateOf(0) }

    val minWidth = 260.dp
    val buttonHeight = 90.dp
    val longPressDurationMs = 2000

    val progress = remember { Animatable(0f) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val baseColor = MaterialTheme.colorScheme.primaryContainer
    val progressColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val shape = MaterialTheme.shapes.extraLarge

    // Flash feedback when command fires
    val flashProgress = remember { Animatable(0f) }
    val flashAlpha = (1f - flashProgress.value) * 0.22f
    val flashScale = 1f + flashProgress.value * 0.18f

    LaunchedEffect(isPressed) {
        if (isPressed) {
            hasTriggered = false
            progress.snapTo(0f)
            val start = System.currentTimeMillis()
            while (isPressed) {
                val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(0)
                val p = (elapsed.toFloat() / longPressDurationMs).coerceIn(0f, 1f)
                progress.snapTo(p)

                if (!hasTriggered && p >= 1f) {
                    hasTriggered = true
                    onLongPressOverOneSecond()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    flashToken++
                    break
                }
                delay(16)
            }
        } else {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(flashToken) {
        if (flashToken == 0) return@LaunchedEffect
        flashProgress.snapTo(0f)
        flashProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 240, easing = LinearEasing)
        )
    }

    Box(
        modifier = modifier
            .height(buttonHeight)
            .widthIn(min = minWidth),
        contentAlignment = Alignment.Center,
    ) {
        // Flash overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = flashScale,
                    scaleY = flashScale,
                    alpha = flashAlpha,
                )
                .clip(shape)
                .background(MaterialTheme.colorScheme.primary)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(shape)
                .background(baseColor)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            val released = tryAwaitRelease()
                            isPressed = false
                            if (!released) isPressed = false
                        }
                    )
                }
        ) {
            // progress fill
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(progressColor)
                    .graphicsLayer(scaleX = progress.value, transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f))
            )

            Row(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    painter = if (isEngineOn)
                        painterResource(R.drawable.engine_stop_fan_512_vector)
                    else
                        painterResource(R.drawable.engine_start_fan_512_vector),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(34.dp),
                )

                Text(
                    text = if (isEngineOn) "Остановить" else "Запуск",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
