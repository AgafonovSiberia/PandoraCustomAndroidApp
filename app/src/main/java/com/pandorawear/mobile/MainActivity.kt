package com.pandorawear.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pandorawear.mobile.infra.storage.*
import com.pandorawear.mobile.theme.PandoraWearTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivity : ComponentActivity() {

    private lateinit var backendConfigStorage: BackendConfigStorage
    private lateinit var deviceCredentialsStorage: DeviceCredentialsStorage



    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        backendConfigStorage = SharedPrefsBackendConfigStorage(applicationContext)
        deviceCredentialsStorage = SharedPrefsDeviceCredentialsStorage(applicationContext)

        val initialConfig = backendConfigStorage.load()
        val initialHasDevice = deviceCredentialsStorage.load() != null

        setContent {
            PandoraWearTheme {
                AppRoot(
                    initialConfig = initialConfig,
                    initialHasDevice = initialHasDevice,
                    backendConfigStorage = backendConfigStorage,
                    deviceCredentialsStorage = deviceCredentialsStorage,
                )
            }
        }
    }
}
