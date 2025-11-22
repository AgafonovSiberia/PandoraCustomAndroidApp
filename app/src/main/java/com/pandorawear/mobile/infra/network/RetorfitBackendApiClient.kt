package com.pandorawear.mobile.infra.network

import android.util.Log
import com.pandorawear.mobile.models.AlarmActionDto
import com.pandorawear.mobile.models.AlarmCommandRequest
import com.pandorawear.mobile.models.AlarmDeviceUiModel
import com.pandorawear.mobile.models.CredPairRequestDto
import com.pandorawear.mobile.models.toUiModel
import com.pandorawear.mobile.infra.storage.DeviceCredentials

class RetrofitBackendApiClient(
    private val api: BackendApiService
) : BackendApiClient {

    override suspend fun pairDeviceByCode(code: String): DeviceCredentials {
        val response = api.pairDevice(code)

        return DeviceCredentials(
            deviceId = response.deviceId,
            token = response.token
        )
    }

    override suspend fun pairDeviceByCred(
        email: String,
        password: String,
        deviceName: String
    ): DeviceCredentials {
        val body = CredPairRequestDto(email = email, password = password, deviceName = deviceName)
        val resp = api.pairDeviceByCred(body)
        return DeviceCredentials(deviceId = resp.deviceId, token = resp.token)
    }

    override suspend fun getDevices(): List<AlarmDeviceUiModel> {
        val response = api.getDevices()
        return response.map { it.toUiModel() }
    }


    override suspend fun sendAlarmCommand(
        alarmDeviceId: Integer,
        action: AlarmActionDto,
    ) {
        Log.d("WearBridgeService", "sendAlarmCommand $action")
        api.sendAlarmCommand(
            AlarmCommandRequest(
                alarmDeviceId = alarmDeviceId,
                action = action.raw,
            )
        )
    }

    override suspend fun unpairDevice(
        deviceId: String,
    ) {
        Log.d("WearBridgeService", "unpairDevice $deviceId")
        api.unpairDevice(
                deviceId = deviceId
            )
    }
}
