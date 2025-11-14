package com.pandorawear.mobile.data.network

import com.pandorawear.mobile.data.storage.DeviceCredentials

class RetrofitBackendApiClient(
    private val api: BackendApiService
) : BackendApiClient {

    override suspend fun pairDevice(code: String): DeviceCredentials {
        val response = api.pairDevice(code)

        return DeviceCredentials(
            deviceId = response.deviceId,
            token = response.token
        )
    }
}
