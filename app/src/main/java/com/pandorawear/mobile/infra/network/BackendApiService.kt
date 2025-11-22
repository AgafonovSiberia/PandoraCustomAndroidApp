package com.pandorawear.mobile.infra.network


import com.pandorawear.mobile.models.AlarmCommandRequest
import com.pandorawear.mobile.models.CredPairRequestDto
import com.pandorawear.mobile.models.PairResponseDto
import com.pandorawear.mobile.models.PandoraDeviceDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
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

    @DELETE("/api/devices/{device_id}")
    suspend fun unpairDevice(
        @Path("device_id") deviceId: String
    )

    @POST("/api/alarm/command")
    suspend fun sendAlarmCommand(
        @Body body: AlarmCommandRequest
    )


}
