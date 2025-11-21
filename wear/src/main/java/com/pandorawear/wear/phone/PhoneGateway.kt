package com.pandorawear.wear.phone

import com.pandorawear.wear.models.PandoraCommand
import com.pandorawear.wear.models.WatchPandoraStatus


interface PhoneGateway {

    suspend fun requestStatus(): Result<WatchPandoraStatus>
    suspend fun sendCommand(command: PandoraCommand, alarmDeviceId: Int): Result<WatchPandoraStatus>
}