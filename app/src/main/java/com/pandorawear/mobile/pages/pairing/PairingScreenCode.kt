package com.pandorawear.mobile.pages.pairing

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pandorawear.mobile.infra.network.BackendApiClient
import com.pandorawear.mobile.infra.storage.DeviceCredentialsStorage
import kotlinx.coroutines.launch

@Composable
fun PairingByCodeForm(
    backendApiClient: BackendApiClient?,
    credentialsStorage: DeviceCredentialsStorage,
    onDevicePaired: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var successText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Сопряжение по коду",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Введите код, который отображается в веб-админке PandoraWear.",
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
                        val credentials = backendApiClient.pairDeviceByCode(code.trim())
                        credentialsStorage.save(credentials)
                        successText = "Устройство успешно сопряжено"
                        onDevicePaired()
                    } catch (e: Exception) {
                        Log.e(TAG, "pairByCode failed", e)
                        errorText =
                            "Не удалось выполнить сопряжение. Проверьте код и повторите попытку."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = code.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Pair device by code")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        errorText?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        successText?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
