package com.pandorawear.mobile.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class BackendStatus {
    Unknown,
    Checking,
    Ok,
    Error,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentHost: String,
    currentPort: String,
    backendReady: Boolean,
    onConfigChanged: (String, String) -> Unit,
    onCheckBackend: suspend (String, String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var host by remember { mutableStateOf(currentHost) }
    var port by remember { mutableStateOf(currentPort) }

    var savedHost by remember { mutableStateOf(currentHost) }
    var savedPort by remember { mutableStateOf(currentPort) }

    // Ошибка формата host (протокол)
    var hostError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentHost, currentPort) {
        host = currentHost
        port = currentPort
        savedHost = currentHost
        savedPort = currentPort
        hostError = null
    }

    var backendStatus by remember {
        mutableStateOf(
            if (backendReady && currentHost.isNotBlank()) {
                BackendStatus.Ok
            } else {
                BackendStatus.Unknown
            }
        )
    }
    var isChecking by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun validateHost(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return if (
            trimmed.startsWith("http://") ||
            trimmed.startsWith("https://")
        ) {
            null
        } else {
            "Адрес должен начинаться с http:// или https://"
        }
    }

    LaunchedEffect(host, port) {
        val trimmedHost = host.trim()
        val trimmedPort = port.trim()

        // если host пустой или явно невалиден — не проверяем backend
        val validationError = validateHost(host)
        hostError = validationError

        if (trimmedHost.isBlank() || validationError != null) {
            backendStatus = BackendStatus.Unknown
            isChecking = false
            return@LaunchedEffect
        }

        isChecking = true
        backendStatus = BackendStatus.Checking

        // debounce, чтобы не стрелять на каждый символ
        delay(600)

        val ok = try {
            onCheckBackend(trimmedHost, trimmedPort)
        } catch (_: Exception) {
            false
        }

        isChecking = false
        backendStatus = if (ok) BackendStatus.Ok else BackendStatus.Error
    }

    val trimmedHost = host.trim()
    val trimmedPort = port.trim()
    val hasChanges = trimmedHost != savedHost || trimmedPort != savedPort


    val canSave = hasChanges &&
            trimmedHost.isNotBlank() &&
            hostError == null

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Подключение к серверу",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    OutlinedTextField(
                        value = host,
                        onValueChange = { newValue ->
                            host = newValue
                            hostError = validateHost(newValue)
                        },
                        label = { Text("Адрес сервера") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Cloud,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
                        isError = hostError != null,
                        supportingText = {
                            Text(
                                text = hostError
                                    ?: "Пример: https://api.example.com",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Порт") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Dns,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Строка статуса backend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val (statusIcon, statusColor, statusText) = when (backendStatus) {
                            BackendStatus.Unknown -> Triple(
                                Icons.Outlined.HelpOutline,
                                MaterialTheme.colorScheme.outline,
                                "Состояние неизвестно",
                            )

                            BackendStatus.Checking -> Triple(
                                Icons.Outlined.Sync,
                                MaterialTheme.colorScheme.primary,
                                "Проверяем соединение…",
                            )

                            BackendStatus.Ok -> Triple(
                                Icons.Outlined.CloudDone,
                                MaterialTheme.colorScheme.primary,
                                "Бэкенд доступен",
                            )

                            BackendStatus.Error -> Triple(
                                Icons.Outlined.CloudOff,
                                MaterialTheme.colorScheme.error,
                                "Бэкенд недоступен",
                            )
                        }

                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        when {
                            isChecking -> {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier
                                        .height(18.dp)
                                        .width(18.dp),
                                )
                            }

                            else -> {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            val h = host.trim()
                                            val p = port.trim()

                                            val validationError = validateHost(host)
                                            hostError = validationError

                                            if (h.isBlank() || validationError != null) {
                                                backendStatus = BackendStatus.Unknown
                                                return@launch
                                            }

                                            isChecking = true
                                            backendStatus = BackendStatus.Checking

                                            val ok = try {
                                                onCheckBackend(h, p)
                                            } catch (_: Exception) {
                                                false
                                            }

                                            isChecking = false
                                            backendStatus =
                                                if (ok) BackendStatus.Ok else BackendStatus.Error
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Проверить соединение",
                                    )
                                }
                            }
                        }
                    }

                    // Кнопка сохранения — внутри блока настроек
                    Button(
                        onClick = {
                            val h = host.trim()
                            val p = port.trim()
                            onConfigChanged(h, p)
                            savedHost = h
                            savedPort = p
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сохранить настройки")
                    }
                }
            }
        }
    }
}
