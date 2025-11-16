package com.pandorawear.mobile.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandorawear.mobile.infra.network.BackendApiClient
import com.pandorawear.mobile.dto.toUiModel
import com.pandorawear.mobile.dto.AlarmDeviceUiModel
import kotlinx.coroutines.launch

data class PandoraUiState(
    val isLoading: Boolean = false,
    val devices: List<AlarmDeviceUiModel> = emptyList(),
    val error: String? = null,
)

class PandoraViewModel(
    private val api: BackendApiClient,
) : ViewModel() {

    var uiState by mutableStateOf(PandoraUiState())
        private set

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = api.getDevices()
                uiState = uiState.copy(
                    isLoading = false,
                    devices = response,
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка загрузки устройств",
                )
            }
        }
    }
}
