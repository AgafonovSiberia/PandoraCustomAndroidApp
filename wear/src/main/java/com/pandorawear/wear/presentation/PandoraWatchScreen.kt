package com.pandorawear.wear.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.pandorawear.wear.models.PandoraCommand

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

        Text(
            text = status.engineTemp?.let { "${it}°C" } ?: "Температура: —",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = status.batteryVoltage?.let { "АКБ: ${it}V" } ?: "АКБ: —",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = if (engineRunning) onStopClick else onStartClick,
        ) {
//            Icon(
//                imageVector = Icons.Filled.Refresh,
//                contentDescription = null,
//            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (engineRunning) "Стоп" else "Старт",
                textAlign = TextAlign.Center,
            )
        }
    }
}
