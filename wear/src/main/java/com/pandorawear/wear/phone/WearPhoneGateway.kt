package com.pandorawear.wear.phone

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.pandorawear.wear.models.PandoraCommand
import com.pandorawear.wear.models.WatchPandoraStatus
import com.pandorawear.wear.models.dto.CommandRequestPayload
import com.pandorawear.wear.models.dto.CommandResponsePayload
import com.pandorawear.wear.models.dto.StatusDto
import com.pandorawear.wear.models.dto.StatusRequestPayload
import com.pandorawear.wear.models.dto.StatusResponsePayload
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WearPhoneGateway(
    private val context: Context,
    private val timeoutMillis: Long = 5_000L,
) : PhoneGateway, MessageClient.OnMessageReceivedListener {

    private val messageClient: MessageClient by lazy {
        Wearable.getMessageClient(context.applicationContext)
    }

    private val nodeClient by lazy {
        Wearable.getNodeClient(context.applicationContext)
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val statusResponseAdapter: JsonAdapter<StatusResponsePayload> by lazy {
        moshi.adapter(StatusResponsePayload::class.java)
    }

    private val commandResponseAdapter: JsonAdapter<CommandResponsePayload> by lazy {
        moshi.adapter(CommandResponsePayload::class.java)
    }

    private val statusRequestAdapter: JsonAdapter<StatusRequestPayload> by lazy {
        moshi.adapter(StatusRequestPayload::class.java)
    }

    private val commandRequestAdapter: JsonAdapter<CommandRequestPayload> by lazy {
        moshi.adapter(CommandRequestPayload::class.java)
    }

    private val _currentStatus = MutableStateFlow<WatchPandoraStatus?>(null)
    override val currentStatus = _currentStatus.asStateFlow()

    private val pendingCommandRequests =
        ConcurrentHashMap<String, CompletableDeferred<CommandResponsePayload>>()

    @Volatile
    private var cachedPhoneNodeId: String? = null

    init {
        messageClient.addListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            PhoneMessagePath.STATUS_RESPONSE -> handleStatusResponse(messageEvent)
            PhoneMessagePath.COMMAND_RESPONSE -> handleCommandResponse(messageEvent)
        }
    }

    private fun handleStatusResponse(messageEvent: MessageEvent) {
        val json = messageEvent.data.toString(StandardCharsets.UTF_8)
        val payload = statusResponseAdapter.fromJson(json) ?: return
        
        _currentStatus.value = payload.status.toDomain()
    }

    private fun handleCommandResponse(messageEvent: MessageEvent) {
        val json = messageEvent.data.toString(StandardCharsets.UTF_8)
        val payload = commandResponseAdapter.fromJson(json) ?: return
        val requestId = payload.requestId ?: return

        payload.status?.let {
            _currentStatus.value = it.toDomain()
        }

        val deferred = pendingCommandRequests.remove(requestId)
        deferred?.complete(payload)
    }

    override suspend fun tryRefreshStatus(): Result<Unit> {
        return runCatching {
            val nodeId = getOrResolvePhoneNodeId()
                ?: throw IllegalStateException("Телефон не найден")

            val requestId = UUID.randomUUID().toString()
            
            val payload = StatusRequestPayload(
                requestId = requestId,
            )

            val json = statusRequestAdapter.toJson(payload)
            val bytes = json.toByteArray(StandardCharsets.UTF_8)

            messageClient
                .sendMessage(nodeId, PhoneMessagePath.STATUS_GET, bytes)
                .await()
            
            Unit
        }
    }

    override suspend fun sendCommand(command: PandoraCommand, alarmDeviceId: Integer): Result<WatchPandoraStatus> {
        return runCatching {
            val nodeId = getOrResolvePhoneNodeId()
                ?: throw IllegalStateException("Телефон не найден")

            Log.d("WearPhoneGateway", "sendCommand: command=$command, alarmDeviceId=$alarmDeviceId, nodeId=$nodeId")

            val requestId = UUID.randomUUID().toString()
            val deferred = CompletableDeferred<CommandResponsePayload>()
            pendingCommandRequests[requestId] = deferred

            val payload = CommandRequestPayload(
                alarmDeviceId = alarmDeviceId,
                requestId = requestId,
                action = when (command) {
                    PandoraCommand.START -> "START"
                    PandoraCommand.STOP -> "STOP"
                },
            )
            val json = commandRequestAdapter.toJson(payload)
            val bytes = json.toByteArray(StandardCharsets.UTF_8)

            messageClient
                .sendMessage(nodeId, PhoneMessagePath.COMMAND, bytes)
                .await()

            val response = try {
                withTimeout(timeoutMillis) {
                    deferred.await()
                }
            } catch (e: TimeoutCancellationException) {
                pendingCommandRequests.remove(requestId)
                throw IllegalStateException("RequestTimeout", e)
            }

            if (!response.success) {
                val errorCode = response.status?.error ?: "UNKNOWN_ERROR"
                throw IllegalStateException("Error command: $errorCode")
            }

            val statusDto = response.status
                ?: throw IllegalStateException("Пустое поле status в ответе на команду")
            
            val domainStatus = statusDto.toDomain()
            // Update cache here too just in case
            _currentStatus.value = domainStatus

            domainStatus
        }
    }

    private suspend fun getOrResolvePhoneNodeId(): String? {
        cachedPhoneNodeId?.let { return it }

        val nodes = nodeClient.connectedNodes.await()
        val node = nodes.firstOrNull()
            ?: return null

        cachedPhoneNodeId = node.id
        return node.id
    }
}

private fun StatusDto.toDomain(): WatchPandoraStatus {
    return WatchPandoraStatus(
        alarmDeviceId = alarmDeviceId,
        isReady = isReady,
        name = name,
        engineTemp = engineTemp,
        cabinTemp = cabinTemp,
        batteryVoltage = batteryVoltage,
        engineRunning = engineRunning,
        errorMsg = error
    )
}