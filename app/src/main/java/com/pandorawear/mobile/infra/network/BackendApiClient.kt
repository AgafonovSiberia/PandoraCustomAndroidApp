package com.pandorawear.mobile.infra.network


import com.pandorawear.mobile.infra.storage.DeviceCredentials

interface BackendApiClient {
    suspend fun pairDeviceByCode(code: String): DeviceCredentials

    suspend fun pairDeviceByCred(email: String, password: String): DeviceCredentials
}
