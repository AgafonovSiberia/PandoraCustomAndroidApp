package com.pandorawear.wear.models

data class WatchPandoraStatus(
    val alarmDeviceId: Int?,
    val isReady: Boolean,
    val carName: String?,
    val temperature: Float?,
    val batteryVoltage: Float?,
    val engineRunning: Boolean?,
    val lastUpdateMillis: Long?,
    val errorCode: String?,
    val errorMessage: String?,
)