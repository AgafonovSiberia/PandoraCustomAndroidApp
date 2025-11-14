package com.pandorawear.mobile.data.network

import com.squareup.moshi.Json

data class PairResponseDto(
    @Json(name = "device_id")
    val deviceId: String,

    @Json(name = "token")
    val token: String,
)