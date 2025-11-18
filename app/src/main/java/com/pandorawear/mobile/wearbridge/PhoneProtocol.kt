package com.pandorawear.mobile.wearbridge
import com.squareup.moshi.Json

object WearBridgePaths {
    const val STATUS_GET = "/pandora/status/get"
    const val STATUS_RESPONSE = "/pandora/status/response"
    const val COMMAND = "/pandora/command"
    const val COMMAND_RESPONSE = "/pandora/command/response"

    const val PROTOCOL_VERSION: Int = 1
}

data class StatusRequestPayload(
    @Json(name = "protocol_version") val protocolVersion: Int,
    @Json(name = "request_id") val requestId: String,
)

data class StatusResponsePayload(
    @Json(name = "protocol_version") val protocolVersion: Int,
    @Json(name = "request_id") val requestId: String?,
    @Json(name = "status") val status: StatusDto?,
)

data class StatusDto(
    @Json(name = "alarm_device_id") val alarmDeviceId: Integer?,
    @Json(name = "is_ready") val isReady: Boolean,
    @Json(name = "car_name") val carName: String?,
    @Json(name = "temperature") val temperature: Double?,
    @Json(name = "battery_voltage") val batteryVoltage: Double?,
    @Json(name = "engine_running") val engineRunning: Boolean?,
    @Json(name = "last_update_ms") val lastUpdateMillis: Long?,
    @Json(name = "error") val error: String?,
)

data class CommandRequestPayload(
    @Json(name = "protocol_version") val protocolVersion: Int,
    @Json(name = "alarm_device_id") val alarmDeviceId: Integer,
    @Json(name = "request_id") val requestId: String,
    @Json(name = "action") val action: String,
)

data class CommandResponsePayload(
    @Json(name = "protocol_version") val protocolVersion: Int,
    @Json(name = "request_id") val requestId: String?,
    @Json(name = "success") val success: Boolean,
    @Json(name = "error") val error: String?,
    @Json(name = "status") val status: StatusDto?,
)
