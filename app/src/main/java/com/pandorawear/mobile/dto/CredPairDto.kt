package com.pandorawear.mobile.dto

import com.squareup.moshi.Json

data class CredPairRequestDto(
    @field:Json(name = "email") val email: String,
    @field:Json(name = "password") val password: String,
)