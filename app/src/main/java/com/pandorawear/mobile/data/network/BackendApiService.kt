package com.pandorawear.mobile.data.network


import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApiService {

    @POST("/api/devices/pairing/{code}")
    suspend fun pairDevice(
        @Path("code") code: String
    ): PairResponseDto
}