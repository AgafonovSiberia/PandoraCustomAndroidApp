package com.pandorawear.mobile.infra.network


import com.pandorawear.mobile.dto.AlarmActionDto
import com.pandorawear.mobile.dto.AlarmCommandRequest
import com.pandorawear.mobile.dto.CredPairRequestDto
import com.pandorawear.mobile.dto.PairResponseDto
import com.pandorawear.mobile.dto.PandoraDeviceDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApiService {

    @POST("/api/devices/pairing/code/{code}")
    suspend fun pairDevice(
        @Path("code") code: String
    ): PairResponseDto


    @GET("/api/alarm/devices")
    suspend fun getDevices(): List<PandoraDeviceDto>

    @POST("/api/devices/pairing/cred")
    suspend fun pairDeviceByCred(
        @Body body: CredPairRequestDto
    ): PairResponseDto

    @POST("/api/alarm/command")
    suspend fun sendAlarmCommand(
        @Body body: AlarmCommandRequest
    ) {
    }
}
