package com.pandorawear.mobile.infra.network

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
        if (isPairingRequest) {
            return chain.proceed(original)
        }

        val creds = credentialsStorage.load() ?: return chain.proceed(original)

        val newRequest = original.newBuilder()
            .header("Authorization", "Bearer ${creds.token}")
            .header("Cookie", "device_id=${creds.deviceId}")
            .build()

        return chain.proceed(newRequest)
    }
}