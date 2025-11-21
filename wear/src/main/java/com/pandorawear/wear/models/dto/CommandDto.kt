package com.pandorawear.wear.models.dto

import com.squareup.moshi.Json

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
