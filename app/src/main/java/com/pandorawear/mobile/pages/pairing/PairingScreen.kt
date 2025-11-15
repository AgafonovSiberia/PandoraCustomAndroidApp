package com.pandorawear.mobile.pages.pairing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pandorawear.mobile.AppState
import com.pandorawear.mobile.infra.network.BackendApiClient
import com.pandorawear.mobile.infra.storage.DeviceCredentialsStorage

private enum class PairingMode {
    BY_CODE,
    BY_EMAIL,
}

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
                PairingModesContainer(
                    backendApiClient = backendApiClient,
                    credentialsStorage = credentialsStorage,
                    onDevicePaired = onDevicePaired,
                )
            }
            AppState.BACKEND_READY_WITH_DEVICE -> {
                DeviceAlreadyPairedState(
                    credentialsStorage = credentialsStorage
                )
            }
        }
    }
}

@Composable
private fun PairingModesContainer(
    backendApiClient: BackendApiClient?,
    credentialsStorage: DeviceCredentialsStorage,
    onDevicePaired: () -> Unit,
) {
    var mode by remember { mutableStateOf(PairingMode.BY_CODE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        val tabs = listOf("По коду", "По email")
        val selectedIndex = when (mode) {
            PairingMode.BY_CODE -> 0
            PairingMode.BY_EMAIL -> 1
        }

        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
                        mode = when (index) {
                            0 -> PairingMode.BY_CODE
                            else -> PairingMode.BY_EMAIL
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (mode) {
            PairingMode.BY_CODE -> {
                PairingByCodeForm(
                    backendApiClient = backendApiClient,
                    credentialsStorage = credentialsStorage,
                    onDevicePaired = onDevicePaired,
                )
            }
            PairingMode.BY_EMAIL -> {
                PairingByEmailForm(
                    backendApiClient = backendApiClient,
                    credentialsStorage = credentialsStorage,
                    onDevicePaired = onDevicePaired,
                )
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
private fun DeviceAlreadyPairedState(
    modifier: Modifier = Modifier,
    credentialsStorage: DeviceCredentialsStorage
) {
    val credentials = credentialsStorage.load()
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = "Устройство сопряжено",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = "DeviceId: ${credentials?.deviceId}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}
