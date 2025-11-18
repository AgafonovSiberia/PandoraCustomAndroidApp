package com.pandorawear.mobile.pages.pandora

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.pandorawear.mobile.models.AlarmActionDto
import com.pandorawear.mobile.models.AlarmDeviceUiModel
import com.pandorawear.mobile.infra.network.BackendApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PandoraUiState(
    val isLoading: Boolean = false,
    val devices: List<AlarmDeviceUiModel> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PandoraScreen(
    backendApiClient: BackendApiClient?,
    modifier: Modifier = Modifier,
) {
    var uiState by remember { mutableStateOf(PandoraUiState(isLoading = true)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(backendApiClient) {
        if (backendApiClient == null) {
            uiState = PandoraUiState(
                isLoading = false,
                error = "Бэкенд недоступен",
            )
            return@LaunchedEffect
        }

        while (true) {
            uiState = uiState.copy(
                isLoading = uiState.devices.isEmpty(),
                error = null,
            )

            uiState = try {
                val devices = backendApiClient.getDevices()
                PandoraUiState(
                    isLoading = false,
                    devices = devices,
                    error = null,
                )
            } catch (e: Exception) {
                PandoraUiState(
                    isLoading = false,
                    devices = emptyList(),
                    error = e.message ?: "Ошибка загрузки устройств",
                )
            }

            delay(5_000L)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent,
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.devices.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        text = uiState.error
                            ?: "Устройство авторизовано.\nДанные Pandora пока недоступны.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            else -> {
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { uiState.devices.size }
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val device = uiState.devices[page]

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        DeviceCard(
                            device = device,
                            onEngineConfirmed = {
                                if (backendApiClient == null) return@DeviceCard

                                val action = if (device.engineRpm > 0) {
                                    AlarmActionDto.STOP
                                } else {
                                    AlarmActionDto.START
                                }

                                scope.launch {
                                    try {
                                        backendApiClient.sendAlarmCommand(
                                            alarmDeviceId = device.id,
                                            action = action,
                                        )

                                         val devices = backendApiClient.getDevices()
                                         uiState = uiState.copy(devices = devices)
                                    } catch (e: Exception) {
                                        // TODO: повесим snackbar / toast позже
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
