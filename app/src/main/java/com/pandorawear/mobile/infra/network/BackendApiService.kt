package com.pandorawear.mobile.infra.network


import com.pandorawear.mobile.dto.CredPairRequestDto
import com.pandorawear.mobile.dto.PairResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApiService {

    @POST("/api/devices/pairing/code/{code}")
    suspend fun pairDevice(
        @Path("code") code: String
    ): PairResponseDto



    @POST("/api/devices/pairing/cred")
    suspend fun pairDeviceByCred(
        @Body body: CredPairRequestDto
    ): PairResponseDto
}
