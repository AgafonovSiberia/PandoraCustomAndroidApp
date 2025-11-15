package com.pandorawear.mobile.dto

import com.squareup.moshi.Json

data class PairResponseDto(
    @Json(name = "device_id")
    val deviceId: String,

    @Json(name = "token")
    val token: String,
)