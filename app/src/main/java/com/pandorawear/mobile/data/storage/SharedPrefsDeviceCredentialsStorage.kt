package com.pandorawear.mobile.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.UUID


class SharedPrefsDeviceCredentialsStorage(
    context: Context,
) : DeviceCredentialsStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun save(credentials: DeviceCredentials) {
        prefs.edit {
            putString(KEY_DEVICE_ID, credentials.deviceId)
                .putString(KEY_TOKEN, credentials.token)
        }
    }

    override fun load(): DeviceCredentials? {
        val deviceId = prefs.getString(KEY_DEVICE_ID, null)
        val token = prefs.getString(KEY_TOKEN, null)


        if (deviceId.isNullOrBlank() || token.isNullOrBlank()) {
            return null
        }

        return DeviceCredentials(
            deviceId = deviceId,
            token = token,
        )
    }

    override fun clear() {
        prefs.edit {
            remove(KEY_DEVICE_ID).remove(KEY_TOKEN)
        }
    }

    private companion object {
        const val PREFS_NAME = "device_credentials"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_TOKEN = "token"
    }
}
