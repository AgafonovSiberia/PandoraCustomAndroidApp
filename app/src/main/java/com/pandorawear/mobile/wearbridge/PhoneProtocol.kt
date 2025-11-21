package com.pandorawear.mobile.wearbridge
import com.squareup.moshi.Json

object WearBridgePaths {
    const val STATUS_GET = "/pandora/status/get"
    const val STATUS_RESPONSE = "/pandora/status/response"
    const val COMMAND = "/pandora/command"
    const val COMMAND_RESPONSE = "/pandora/command/response"

}

data class StatusRequestPayload(
     @param:Json(name = "request_id")
    val requestId: String,
)

data class StatusResponsePayload(
    @param:Json(name = "alarm_device_id")
    val alarmDeviceId: Integer?,

    @param:Json(name = "request_id")
    val requestId: String?,

    @param:Json(name = "status")
    val status: StatusDto,
)


data class StatusDto(
    @param:Json(name = "alarm_device_id")
    val alarmDeviceId: Integer?,

    @param:Json(name = "name")
    val name: String?,

    @param:Json(name = "fuel_tank")
    val fuelTank: Int?,

    @param:Json(name = "is_ready")
    val isReady: Boolean?,

    @param:Json(name = "engine_temp")
    val engineTemp: Double?,

    @param:Json(name = "cabinTemp")
    val cabinTemp: Double?,


    @param:Json(name = "voltage")
    val batteryVoltage: Double?,

    @param:Json(name = "engine_running")
    val engineRunning: Boolean?,

    @param:Json(name = "last_update_ms")
    val lastUpdateMillis: Long?,

    @param:Json(name = "error")
    val error: String?

)

data class CommandRequestPayload(
    @param:Json(name = "alarm_device_id")
    val alarmDeviceId: Integer,

    @param:Json(name = "action")
    val action: String,

    @param:Json(name = "request_id")
    val requestId: String,
)

data class CommandResponsePayload(

    @param:Json(name = "alarm_device_id")
    val alarmDeviceId: Integer?,

    @param:Json(name = "success")
    val success: Boolean,

    @param:Json(name = "request_id")
    val requestId: String?,

    @param:Json(name = "status")
    val status: StatusDto?,

    )