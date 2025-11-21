package com.pandorawear.wear.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.pandorawear.wear.models.PandoraCommand
import kotlinx.coroutines.launch

@Composable
fun PandoraWatchScreen(
    viewModel: PandoraWatchViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

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

@Composable
private fun StatusChip(
    icon: ImageVector,
    valueText: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
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
//        Icon(
//            imageVector = Icons.Filled.Warning,
//            contentDescription = null,
//            tint = MaterialTheme.colorScheme.primary,
//        )
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
//        Icon(
//            imageVector = Icons.Filled.Warning,
//            contentDescription = null,
//            tint = MaterialTheme.colorScheme.error,
//        )
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = status.name ?: "Pandora",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatusChip(
            icon = Icons.Rounded.Thermostat,
            valueText = "${status.engineTemp}°C",
            contentDescription = "Температура",
            modifier = Modifier.weight(1f),
        )

        StatusChip(
            icon = Icons.Rounded.BatteryFull,
            valueText = "${status.batteryVoltage}V",
            contentDescription = "Напряжение бортовой сети",
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (state) {
            is PandoraWatchUiState.Ready -> {
                val status = state.status

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {

                    EngineStartBar(
                        isEngineOn = engineRunning,
                        modifier = Modifier.fillMaxWidth(),
                        onConfirmed = {
                            val command = if (engineRunning) {
                                onStartClick()
                            } else {
                                onStopClick()
                            }

                        }
                    )
                }
            }
        }
    }
}
