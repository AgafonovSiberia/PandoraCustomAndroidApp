package com.pandorawear.mobile.pages.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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

    var checkInProgress by remember { mutableStateOf(false) }
    var checkResultText by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "Настройки сервера",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = host,
                onValueChange = {
                    host = it
                    checkResultText = null
                },
                label = { Text("Host") },
                placeholder = { Text("10.0.2.2 или api.example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = port,
                onValueChange = {
                    port = it
                    checkResultText = null
                },
                label = { Text("Port") },
                placeholder = { Text("8000 или пусто") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        val trimmedHost = host.trim()
                        val trimmedPort = port.trim()
                        onConfigChanged(trimmedHost, trimmedPort)
                        checkResultText = "Сохранено, теперь можно проверить хост"
                    },
                    enabled = host.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }

                Button(
                    onClick = {
                        scope.launch {
                            checkInProgress = true
                            checkResultText = null

                            val trimmedHost = host.trim()
                            val trimmedPort = port.trim()

                            val ok = onCheckBackend(trimmedHost, trimmedPort)

                            checkInProgress = false
                            checkResultText = if (ok) {
                                "Бэкенд отвечает 👍"
                            } else {
                                "Не удалось достучаться до /api/ready"
                            }
                        }
                    },
                    enabled = host.isNotBlank() && !checkInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    if (checkInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Проверить хост")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (checkResultText != null) {
                Text(
                    text = checkResultText ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (backendReady) {
                    "Состояние: бэкенд готов ✔"
                } else {
                    "Состояние: бэкенд не проверен / не доступен"
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
