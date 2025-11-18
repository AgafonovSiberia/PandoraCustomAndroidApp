package com.pandorawear.mobile.models

import com.squareup.moshi.Json

data class PairResponseDto(
    @Json(name = "device_id")
    val deviceId: String,

    @Json(name = "token")
    val token: String,
)