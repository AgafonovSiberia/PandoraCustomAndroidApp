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

    companion object {
        private const val ERROR_BACKEND_UNAVAILABLE = "BACKEND_UNAVAILABLE"
        private const val ERROR_NO_DEVICE = "NO_DEVICE"
        private const val ERROR_NOT_READY = "NOT_READY"
    }

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

    private fun applyStatusFromPhone(
        status: WatchPandoraStatus,
    ) {
        lastKnownStatus = status

        if (status.isReady == true) {
            _uiState.value = PandoraWatchUiState.Ready(status)
            return
        }

        val message = when (status.errorMsg) {
            ERROR_BACKEND_UNAVAILABLE ->
                "Бэкенд недоступен. Проверьте настройки сервера в приложении на телефоне."

            ERROR_NO_DEVICE ->
                "Нет привязанного устройства Pandora. Добавьте устройство в приложении на телефоне."

            ERROR_NOT_READY ->
                "Телефон не готов. Откройте приложение Pandora на телефоне."

            else ->
                status.errorMsg ?: "Настройте приложение на телефоне"
        }

        _uiState.value = PandoraWatchUiState.NotReady(
            lastKnownStatus = lastKnownStatus,
            message = message,
        )
    }


    private suspend fun refreshStatusOnce() {
        if (lastKnownStatus == null) {
            _uiState.value = PandoraWatchUiState.Loading
        }

        val result = phoneGateway.requestStatus()

        result
            .onSuccess { status ->
                applyStatusFromPhone(
                    status = status
                )
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
        val deviceId = currentStatus.alarmDeviceId ?: return

        viewModelScope.launch {
            val result = phoneGateway.sendCommand(command = command, alarmDeviceId = deviceId)

            result
                .onSuccess { status ->
                    applyStatusFromPhone(
                        status = status,
                    )
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