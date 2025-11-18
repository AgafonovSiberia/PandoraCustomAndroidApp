package com.pandorawear.wear.models.dto

import com.squareup.moshi.Json

data class CommandRequestPayload(
    @Json(name = "protocol_version") val protocolVersion: Int,
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