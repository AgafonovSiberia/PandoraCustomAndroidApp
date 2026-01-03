package com.pandorawear.wear.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.pandorawear.wear.models.PandoraCommand
import com.pandorawear.wear.R



@Composable
fun PandoraWatchScreen(
    viewModel: PandoraWatchViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Diagonal gradient from dark gray to black
    val backgroundGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2C3E50), // Dark gray-blue
            Color(0xFF1A1A1A), // Very dark gray
            Color(0xFF000000), // Pure black
        ),
        start = Offset(0f, 0f), // Top-left
        end = Offset.Infinite // Bottom-right
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        when (val state = uiState) {
            is PandoraWatchUiState.Loading -> LoadingScreen()
            is PandoraWatchUiState.NotReady -> NotReadyScreen(message = state.message)
            is PandoraWatchUiState.Ready -> ReadyScreen(
                state = state,
                onStartClick = { viewModel.onCommandClicked(PandoraCommand.START) },
                onStopClick = { viewModel.onCommandClicked(PandoraCommand.STOP) },
            )

            is PandoraWatchUiState.Error -> ErrorScreen(message = state.message)
        }
    }
}


@Composable
private fun StatusChip(
    icon: Painter,
    valueText: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Получаем данные…",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun NotReadyScreen(message: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message ?: "Настройте приложение на телефоне",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ErrorScreen(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ReadyScreen(
    state: PandoraWatchUiState.Ready,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    val status = state.status
    val engineRunning = status.engineRunning == true

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Gauge lines for battery and temperature
        DualMetricGaugeLines(
            batteryVoltage = status.batteryVoltage,
            engineTemp = status.engineTemp,
            modifier = Modifier.fillMaxSize(),
        )

        // Circular engine start/stop button in the center
        CircularEngineButton(
            isEngineOn = engineRunning,
            onLongPressOverOneSecond = {
                if (engineRunning) {
                    onStopClick()
                } else {
                    onStartClick()
                }
            },
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
