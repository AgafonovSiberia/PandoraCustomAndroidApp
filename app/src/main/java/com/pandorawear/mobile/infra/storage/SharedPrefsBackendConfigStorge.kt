package com.pandorawear.mobile.infra.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefsBackendConfigStorage(
    context: Context,
) : BackendConfigStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun save(config: BackendConfig) {
        prefs.edit {
            putString(KEY_HOST, config.host)
            putString(KEY_PORT, config.port)
        }
    }

    override fun load(): BackendConfig? {
        val host = prefs.getString(KEY_HOST, null) ?: return null
        val port = prefs.getString(KEY_PORT, "") ?: ""

        if (host.isBlank()) return null

        return BackendConfig(
            host = host,
            port = port,
        )
    }

    override fun clear() {
        prefs.edit {
            remove(KEY_HOST)
            remove(KEY_PORT)
        }
    }

    private companion object {
        const val PREFS_NAME = "backend_config"
        const val KEY_HOST = "host"
        const val KEY_PORT = "port"
    }
}
