package com.pandorawear.mobile.infra.network


import com.pandorawear.mobile.dto.AlarmActionDto
import com.pandorawear.mobile.dto.AlarmDeviceUiModel
import com.pandorawear.mobile.infra.storage.DeviceCredentials

interface BackendApiClient {
    suspend fun pairDeviceByCode(code: String): DeviceCredentials

    suspend fun pairDeviceByCred(email: String, password: String): DeviceCredentials

    suspend fun getDevices(): List<AlarmDeviceUiModel>

    suspend fun sendAlarmCommand(
        alarmDeviceId: Integer,
        action: AlarmActionDto,
    ) {
    }

}
