package com.pandorawear.wear.models

data class WatchPandoraStatus(
    val alarmDeviceId: Integer?,
    val isReady: Boolean?,
    val name: String?,
    val engineTemp: Double?,
    val cabinTemp: Double?,
    val batteryVoltage: Double?,
    val engineRunning: Boolean?,
    val errorMsg: String?

)