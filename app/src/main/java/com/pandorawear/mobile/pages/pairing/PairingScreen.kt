package com.pandorawear.mobile.pages.pairing

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pandorawear.mobile.AppState
import com.pandorawear.mobile.data.network.BackendApiClient

import com.pandorawear.mobile.data.storage.DeviceCredentialsStorage
import kotlinx.coroutines.launch

@Composable
fun PairingScreen(
    appState: AppState,
    backendApiClient: BackendApiClient?,
    credentialsStorage: DeviceCredentialsStorage,
    onDevicePaired: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        when (appState) {
            AppState.BACKEND_UNAVAILABLE -> {
                NoBackendConfiguredState(onOpenSettings = onOpenSettings)
            }
            AppState.BACKEND_AVAILABLE_NO_DEVICE -> {
                PairingFormState(
                    backendApiClient = backendApiClient,
                    credentialsStorage = credentialsStorage,
                    onDevicePaired = onDevicePaired,
                )
            }
            AppState.BACKEND_READY_WITH_DEVICE -> {
                DeviceAlreadyPairedState()
            }
        }
    }
}


@Composable
private fun NoBackendConfiguredState(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = "Сначала настрой сервер",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Укажите адрес backend-а во вкладке «Настройки», затем вернитесь к сопряжению.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onOpenSettings) {
                Text("Перейти в настройки")
            }
        }
    }
}


@Composable
private fun PairingFormState(
    backendApiClient: BackendApiClient?,
    credentialsStorage: DeviceCredentialsStorage,
    onDevicePaired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var successText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "Сопряжение устройства",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Введите код сопряжения, который отображается в веб-админке PandoraWear.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it
                    errorText = null
                    successText = null
                },
                singleLine = true,
                label = { Text("Код сопряжения") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (backendApiClient == null) {
                        errorText = "Клиент backend-а недоступен"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorText = null
                        successText = null

                        try {
                            val credentials = backendApiClient.pairDevice(code.trim())

                            credentialsStorage.save(credentials)
                            successText = "Устройство успешно сопряжено"
                            onDevicePaired()
                        } catch (e: Exception) {
                            errorText =
                                "Не удалось выполнить сопряжение. Проверьте код и повторите попытку ${e.toString()}"

                            Log.i(TAG, "🟦 Checking backend health at URL: $e")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = code.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Сопрячь устройство")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (errorText != null) {
                Text(
                    text = errorText ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }

            if (successText != null) {
                Text(
                    text = successText ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}


@Composable
private fun DeviceAlreadyPairedState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = "Устройство уже сопряжено",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Откройте вкладку Pandora, чтобы посмотреть сведения об устройстве.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
