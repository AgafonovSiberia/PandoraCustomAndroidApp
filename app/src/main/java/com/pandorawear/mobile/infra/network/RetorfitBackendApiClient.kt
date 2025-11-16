package com.pandorawear.mobile.infra.network

import com.pandorawear.mobile.dto.AlarmActionDto
import com.pandorawear.mobile.dto.AlarmCommandRequest
import com.pandorawear.mobile.dto.AlarmDeviceUiModel
import com.pandorawear.mobile.dto.CredPairRequestDto
import com.pandorawear.mobile.dto.toUiModel
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

    override suspend fun pairDeviceByCred(email: String, password: String): DeviceCredentials {
        val resp = api.pairDeviceByCred(
            CredPairRequestDto(email = email, password = password)
        )
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
        api.sendAlarmCommand(
            AlarmCommandRequest(
                alarmDeviceId = alarmDeviceId,
                action = action.raw,
            )
        )
    }
}
