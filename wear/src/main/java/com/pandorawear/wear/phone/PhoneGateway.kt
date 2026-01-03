package com.pandorawear.wear.phone

import com.pandorawear.wear.models.PandoraCommand
import com.pandorawear.wear.models.WatchPandoraStatus
import kotlinx.coroutines.flow.StateFlow

interface PhoneGateway {

    val currentStatus: StateFlow<WatchPandoraStatus?>

    suspend fun tryRefreshStatus(): Result<Unit>
    suspend fun sendCommand(command: PandoraCommand, alarmDeviceId: Integer): Result<WatchPandoraStatus>
}