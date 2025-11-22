package com.pandorawear.mobile.pages.pairing

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pandorawear.mobile.infra.network.BackendApiClient
import com.pandorawear.mobile.infra.storage.DeviceCredentialsStorage

import kotlinx.coroutines.launch

@Composable
fun PairingByEmailForm(
    backendApiClient: BackendApiClient?,
    credentialsStorage: DeviceCredentialsStorage,
    onDevicePaired: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var successText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Сопряжение по email",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Используйте данные пользователя, зарегистрированного в PandoraCustomApiServer",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorText = null
                successText = null
            },
            singleLine = true,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorText = null
                successText = null
            },
            singleLine = true,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        val canSubmit = email.isNotBlank() && password.isNotBlank() && !isLoading

        Button(
            onClick = {
                if (backendApiClient == null) {
                    errorText = "Сервер недоступен"
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    errorText = null
                    successText = null

                    try {
                        val credentials = backendApiClient.pairDeviceByCred(
                            email = email.trim(),
                            password = password,
                            deviceName = android.os.Build.MODEL
                        )
                        credentialsStorage.save(credentials)
                        successText = "Устройство успешно сопряжено"
                        onDevicePaired()
                    } catch (e: Exception) {
                        Log.e(TAG, "pairByEmail failed", e)
                        errorText =
                            "Не удалось выполнить сопряжение. Проверьте данные и повторите."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = canSubmit,
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
                Text("Сопряжение")
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
