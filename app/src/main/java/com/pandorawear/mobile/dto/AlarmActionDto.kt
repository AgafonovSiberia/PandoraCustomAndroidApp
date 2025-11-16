package com.pandorawear.mobile.dto

import com.squareup.moshi.Json

enum class AlarmActionDto(val raw: String) {
    START("start"),
    STOP("stop"),
}

data class AlarmCommandRequest(
    @param:Json(name = "alarm_device_id")
    val alarmDeviceId: Integer,

    @param:Json(name = "action")
    val action: String,
)