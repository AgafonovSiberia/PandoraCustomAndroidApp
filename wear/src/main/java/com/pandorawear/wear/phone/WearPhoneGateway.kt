package com.pandorawear.wear.phone

import android.content.Context
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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WearPhoneGateway(
    private val context: Context,
    private val timeoutMillis: Long = 3_000L,
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

    private val pendingStatusRequests =
        ConcurrentHashMap<String, CompletableDeferred<StatusResponsePayload>>()

    private val pendingCommandRequests =
        ConcurrentHashMap<String, CompletableDeferred<CommandResponsePayload>>()

    @Volatile
    private var cachedPhoneNodeId: String? = null

    init {
        // Регистрируемся как listener один раз на весь жизненный цикл gateway.
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
        val requestId = payload.requestId ?: return

        val deferred = pendingStatusRequests.remove(requestId)
        deferred?.complete(payload)
    }

    private fun handleCommandResponse(messageEvent: MessageEvent) {
        val json = messageEvent.data.toString(StandardCharsets.UTF_8)
        val payload = commandResponseAdapter.fromJson(json) ?: return
        val requestId = payload.requestId ?: return

        val deferred = pendingCommandRequests.remove(requestId)
        deferred?.complete(payload)
    }

    override suspend fun requestStatus(): Result<WatchPandoraStatus> {
        return runCatching {
            val nodeId = getOrResolvePhoneNodeId()
                ?: throw IllegalStateException("Телефон не найден (нет подключённых узлов)")

            val requestId = UUID.randomUUID().toString()
            val deferred = CompletableDeferred<StatusResponsePayload>()
            pendingStatusRequests[requestId] = deferred

            val payload = StatusRequestPayload(
                protocolVersion = PhoneMessagePath.PROTOCOL_VERSION,
                requestId = requestId,
            )

            val json = statusRequestAdapter.toJson(payload)
            val bytes = json.toByteArray(StandardCharsets.UTF_8)

            // Отправляем запрос
            messageClient
                .sendMessage(nodeId, PhoneMessagePath.STATUS_GET, bytes)
                .await()

            // Ждём ответ с тайм-аутом
            val response = try {
                withTimeout(timeoutMillis) {
                    deferred.await()
                }
            } catch (e: TimeoutCancellationException) {
                pendingStatusRequests.remove(requestId)
                throw IllegalStateException("Тайм-аут при ожидании ответа статуса", e)
            }

            if (response.protocolVersion != PhoneMessagePath.PROTOCOL_VERSION) {
                throw IllegalStateException("Несовместимая версия протокола: ${response.protocolVersion}")
            }

            val statusDto = response.status
                ?: throw IllegalStateException("Пустое поле status в ответе телефона")

            statusDto.toDomain()
        }
    }

    override suspend fun sendCommand(command: PandoraCommand): Result<WatchPandoraStatus> {
        return runCatching {
            val nodeId = getOrResolvePhoneNodeId()
                ?: throw IllegalStateException("Телефон не найден (нет подключённых узлов)")

            val requestId = UUID.randomUUID().toString()
            val deferred = CompletableDeferred<CommandResponsePayload>()
            pendingCommandRequests[requestId] = deferred

            val payload = CommandRequestPayload(
                protocolVersion = PhoneMessagePath.PROTOCOL_VERSION,
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
                throw IllegalStateException("Тайм-аут при ожидании ответа на команду", e)
            }

            if (response.protocolVersion != PhoneMessagePath.PROTOCOL_VERSION) {
                throw IllegalStateException("Несовместимая версия протокола: ${response.protocolVersion}")
            }

            if (!response.success) {
                val errorCode = response.error ?: "UNKNOWN_ERROR"
                throw IllegalStateException("Команда не выполнена: $errorCode")
            }

            val statusDto = response.status
                ?: throw IllegalStateException("Пустое поле status в ответе на команду")

            statusDto.toDomain()
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

/**
 * Маппинг DTO -> доменная модель WatchPandoraStatus
 */
private fun StatusDto.toDomain(): WatchPandoraStatus {
    return WatchPandoraStatus(
        isReady = isReady,
        carName = carName,
        temperature = temperature?.toFloat(),
        batteryVoltage = batteryVoltage?.toFloat(),
        engineRunning = engineRunning,
        lastUpdateMillis = lastUpdateMillis,
        errorCode = error,
        errorMessage = when (error) {
            null -> null
            "NOT_READY" -> "Телефон не готов"
            "BACKEND_UNAVAILABLE" -> "Нет связи с сервером"
            "NO_DEVICE" -> "Устройство не привязано"
            "COMMAND_FAILED" -> "Ошибка при выполнении команды"
            "INCOMPATIBLE_PROTOCOL" -> "Несовместимая версия протокола"
            else -> "Ошибка: $error"
        },
    )
}