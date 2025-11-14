package com.pandorawear.mobile.data.network


import com.pandorawear.mobile.data.storage.DeviceCredentials
import retrofit2.http.POST

interface BackendApiClient {
    suspend fun pairDevice(code: String): DeviceCredentials
}
