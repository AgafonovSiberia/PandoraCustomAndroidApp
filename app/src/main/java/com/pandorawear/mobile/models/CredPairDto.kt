package com.pandorawear.mobile.models

import com.squareup.moshi.Json

data class CredPairRequestDto(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "device_name") val deviceName: String,
)