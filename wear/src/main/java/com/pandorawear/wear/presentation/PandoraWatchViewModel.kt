package com.pandorawear.wear.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandorawear.wear.models.PandoraCommand
import com.pandorawear.wear.models.WatchPandoraStatus
import com.pandorawear.wear.phone.PhoneGateway
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PandoraWatchViewModel(
    private val phoneGateway: PhoneGateway,
    private val pollingIntervalMillis: Long = 10_000L,
) : ViewModel() {

    private val _uiState: MutableStateFlow<PandoraWatchUiState> =
        MutableStateFlow(PandoraWatchUiState.Loading)
    val uiState: StateFlow<PandoraWatchUiState> = _uiState

    private var pollingJob: Job? = null
    private var lastKnownStatus: WatchPandoraStatus? = null

    fun start() {
        if (pollingJob != null) return

        pollingJob = viewModelScope.launch {
            while (true) {
                refreshStatusOnce()
                delay(pollingIntervalMillis)
            }
        }
    }

    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun refreshStatus() {
        viewModelScope.launch {
            refreshStatusOnce()
        }
    }

    private suspend fun refreshStatusOnce() {
        if (_uiState.value is PandoraWatchUiState.Loading) {
            _uiState.value = PandoraWatchUiState.Loading
        }

        val result = phoneGateway.requestStatus()

        result
            .onSuccess { status ->
                lastKnownStatus = status

                status.isReady?.let {
                    if (!it) {
                        _uiState.value = PandoraWatchUiState.NotReady(
                            lastKnownStatus = lastKnownStatus,
                            message = status.errorMsg ?: "Настройте приложение на телефоне",
                        )
                    } else {
                        _uiState.value = PandoraWatchUiState.Ready(status)
                    }
                }
            }
            .onFailure { error ->
                _uiState.value = PandoraWatchUiState.Error(
                    message = error.message ?: "Нет связи с телефоном",
                    lastKnownStatus = lastKnownStatus,
                )
            }
    }

    fun onCommandClicked(command: PandoraCommand) {
        val currentState = _uiState.value
        if (currentState !is PandoraWatchUiState.Ready) {
            return
        }

        val currentStatus = currentState.status
        val deviceId = currentStatus.alarmDeviceId?: return

        viewModelScope.launch {
            val result = phoneGateway.sendCommand(command = command, alarmDeviceId = deviceId)

            result
                .onSuccess { status ->
                    lastKnownStatus = status
                    status.isReady?.let {
                        if (!it) {
                            _uiState.value = PandoraWatchUiState.NotReady(
                                lastKnownStatus = lastKnownStatus,
                                message = status.errorMsg ?: "Телефон не готов",
                            )
                        } else {
                            _uiState.value = PandoraWatchUiState.Ready(status)
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.value = PandoraWatchUiState.Error(
                        message = error.message ?: "Ошибка при выполнении команды",
                        lastKnownStatus = lastKnownStatus,
                    )
                }
        }
    }
}