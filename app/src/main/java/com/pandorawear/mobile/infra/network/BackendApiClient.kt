package com.pandorawear.mobile.infra.network


import com.pandorawear.mobile.models.AlarmActionDto
import com.pandorawear.mobile.models.AlarmDeviceUiModel
import com.pandorawear.mobile.infra.storage.DeviceCredentials
import java.util.UUID
import kotlin.uuid.Uuid

interface BackendApiClient {
    suspend fun pairDeviceByCode(code: String): DeviceCredentials

    suspend fun pairDeviceByCred(
        email: String,
        password: String,
        deviceName: String
    ): DeviceCredentials

    suspend fun getDevices(): List<AlarmDeviceUiModel>

    suspend fun sendAlarmCommand(
        alarmDeviceId: Integer,
        action: AlarmActionDto,
    ) {
    }

    suspend fun unpairDevice(
        deviceId: String
    ) {

    }

}
