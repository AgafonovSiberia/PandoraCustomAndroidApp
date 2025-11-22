package com.pandorawear.mobile.infra.network

import com.pandorawear.mobile.infra.session.SessionEvents
import com.pandorawear.mobile.infra.storage.DeviceCredentialsStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthCookieInterceptor(
    private val credentialsStorage: DeviceCredentialsStorage,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url
        val path = url.encodedPath

        val isPairingRequest = path.startsWith("/api/devices/pairing")

        val request = if (isPairingRequest) {
            original
        } else {
            val credentials = credentialsStorage.load()
            if (credentials != null) {
                original.newBuilder()
                    .header("Authorization", "Bearer ${credentials.token}")
                    .header("Cookie", "device_id=${credentials.deviceId}")
                    .build()
            } else {
                original
            }
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            credentialsStorage.clear()
            SessionEvents.notifyUnauthorized()
        }

        return response
    }
}