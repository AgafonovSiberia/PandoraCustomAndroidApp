package com.pandorawear.mobile

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BluetoothConnected
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.NavigationBarItemDefaults
import com.pandorawear.mobile.data.network.BackendApiClient
import com.pandorawear.mobile.data.network.BackendApiClientFactory
import com.pandorawear.mobile.data.network.BackendHealthChecker
import com.pandorawear.mobile.data.storage.*
import com.pandorawear.mobile.pages.pairing.PairingScreen
import com.pandorawear.mobile.pages.settings.SettingsScreen
import com.pandorawear.mobile.ui.theme.BluePrimary
import com.pandorawear.mobile.ui.theme.SurfaceDark
import com.pandorawear.mobile.ui.theme.TextSecondary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pandorawear.mobile.pages.pandora.PandoraScreen

enum class MainTab {
    PANDORA,
    PAIRING,
    SETTINGS,
}


@Composable
private fun BottomDockBar(
    selectedTab: MainTab,
    pairingEnabled: Boolean,
    pandoraEnabled: Boolean,
    onTabSelected: (MainTab) -> Unit,
    appState: AppState,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true,
            )
            .background(
                color = SurfaceDark.copy(alpha = 0.85f),
                shape = RoundedCornerShape(24.dp),
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
            ) {

                // Pandora (видна всегда, но может быть disabled)
                NavigationBarItem(
                    selected = selectedTab == MainTab.PANDORA,
                    onClick = { onTabSelected(MainTab.PANDORA) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PhoneIphone,
                            contentDescription = "Pandora",
                        )
                    },
                    enabled = pandoraEnabled,
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = BluePrimary,
                        unselectedIconColor = TextSecondary.copy(alpha = 0.5f),
                        disabledIconColor = TextSecondary.copy(alpha = 0.3f),
                    ),
                )

                // Pairing
                NavigationBarItem(
                    selected = selectedTab == MainTab.PAIRING,
                    onClick = { onTabSelected(MainTab.PAIRING) },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.BluetoothConnected,
                            contentDescription = "Сопряжение",
                        )
                    },
                    enabled = pairingEnabled,
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = BluePrimary,
                        unselectedIconColor = TextSecondary.copy(alpha = 0.5f),
                        disabledIconColor = TextSecondary.copy(alpha = 0.3f),
                    ),
                )



                // Settings – всегда доступны
                NavigationBarItem(
                    selected = selectedTab == MainTab.SETTINGS,
                    onClick = { onTabSelected(MainTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Настройки",
                        )
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = BluePrimary,
                        unselectedIconColor = TextSecondary.copy(alpha = 0.5f),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun AppRoot(
    initialConfig: BackendConfig?,
    initialHasDevice: Boolean,
    backendConfigStorage: BackendConfigStorage,
    deviceCredentialsStorage: DeviceCredentialsStorage,
) {
    var host by remember { mutableStateOf(initialConfig?.host ?: "") }
    var port by remember { mutableStateOf(initialConfig?.port ?: "") }

    var backendReady by remember { mutableStateOf(false) }
    var hasDevice by remember { mutableStateOf(initialHasDevice) }

    var appState by remember { mutableStateOf(AppState.BACKEND_UNAVAILABLE) }

    var selectedTab by remember { mutableStateOf(MainTab.SETTINGS) }


    LaunchedEffect(host, port, hasDevice) {
        if (host.isBlank()) {
            backendReady = false
            appState = AppState.BACKEND_UNAVAILABLE
            selectedTab = MainTab.SETTINGS
        } else {
            val ok = BackendHealthChecker.isBackendReady(host, port)
            backendReady = ok

            appState = when {
                !ok -> AppState.BACKEND_UNAVAILABLE
                ok && !hasDevice -> AppState.BACKEND_AVAILABLE_NO_DEVICE
                else -> AppState.BACKEND_READY_WITH_DEVICE
            }

            selectedTab = when (appState) {
                AppState.BACKEND_UNAVAILABLE          -> MainTab.SETTINGS    // правило 1
                AppState.BACKEND_AVAILABLE_NO_DEVICE  -> MainTab.PAIRING     // правило 2.2
                AppState.BACKEND_READY_WITH_DEVICE    -> MainTab.PANDORA     // правило 2.1
            }
        }
    }

    val backendApiClient: BackendApiClient? = remember(host, port, backendReady) {
        if (!backendReady || host.isBlank()) null
        else {
            val rawBase = if (port.isNotBlank()) "${host.trim()}:${port.trim()}" else host.trim()
            runCatching {
                BackendApiClientFactory.create(rawBase, true)
            }.getOrNull()
        }
    }

    val pairingEnabled   = appState != AppState.BACKEND_UNAVAILABLE
    val pandoraEnabled   = appState == AppState.BACKEND_READY_WITH_DEVICE
    val settingsEnabled  = true

    Scaffold(
        bottomBar = {
            BottomDockBar(
                selectedTab = selectedTab,
                pairingEnabled = pairingEnabled,
                pandoraEnabled = pandoraEnabled,
                onTabSelected = { tab ->
                    when (tab) {
                        MainTab.PAIRING  -> if (pairingEnabled)  selectedTab = tab
                        MainTab.PANDORA  -> if (pandoraEnabled)  selectedTab = tab
                        MainTab.SETTINGS -> if (settingsEnabled) selectedTab = tab
                    }
                },
                appState = appState,
            )
        }
    ) { padding ->

        when (selectedTab) {
            MainTab.SETTINGS -> {
                SettingsScreen(
                    currentHost = host,
                    currentPort = port,
                    backendReady = backendReady,
                    onConfigChanged = { newHost, newPort ->
                        host = newHost
                        port = newPort
                        if (newHost.isNotBlank()) {
                            backendConfigStorage.save(BackendConfig(newHost, newPort))
                        } else {
                            backendConfigStorage.clear()
                        }
                    },
                    onCheckBackend = { h, p ->
                        val ok = BackendHealthChecker.isBackendReady(h, p)
                        backendReady = ok
                        ok
                    },
                    modifier = Modifier.padding(padding),
                )
            }

            MainTab.PAIRING -> {
                PairingScreen(
                    appState = appState,
                    backendApiClient = backendApiClient,
                    credentialsStorage = deviceCredentialsStorage,
                    onDevicePaired = {
                        hasDevice = true
                    },
                    onOpenSettings = { selectedTab = MainTab.SETTINGS },
                    modifier = Modifier.padding(padding),
                )
            }

            MainTab.PANDORA -> {
                PandoraScreen(
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

