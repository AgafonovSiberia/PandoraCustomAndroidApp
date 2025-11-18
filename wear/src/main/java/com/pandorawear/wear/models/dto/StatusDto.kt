package com.pandorawear.wear.models.dto

import com.squareup.moshi.Json

data class StatusRequestPayload(
    @Json(name = "protocol_version") val protocolVersion: Int,
    @Json(name = "request_id") val requestId: String,
)

data class StatusResponsePayload(
    @Json(name = "protocol_version") val protocolVersion: Int,
    @Json(name = "alarm_device_id") val alarmDeviceId: Int?,
    @Json(name = "request_id") val requestId: String?,
    @Json(name = "status") val status: StatusDto?,
)

data class StatusDto(
    @Json(name = "alarm_device_id") val alarmDeviceId: Int?,
    @Json(name = "is_ready") val isReady: Boolean,
    @Json(name = "car_name") val carName: String?,
    @Json(name = "temperature") val temperature: Double?,
    @Json(name = "battery_voltage") val batteryVoltage: Double?,
    @Json(name = "engine_running") val engineRunning: Boolean?,
    @Json(name = "last_update_ms") val lastUpdateMillis: Long?,
    @Json(name = "error") val error: String?,
)