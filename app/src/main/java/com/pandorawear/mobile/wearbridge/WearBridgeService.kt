package com.pandorawear.mobile.wearbridge

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.pandorawear.mobile.infra.network.BackendApiClient
import com.pandorawear.mobile.infra.network.BackendApiClientFactory
import com.pandorawear.mobile.infra.network.BackendUrls
import com.pandorawear.mobile.infra.storage.BackendConfigStorage
import com.pandorawear.mobile.infra.storage.DeviceCredentialsStorage
import com.pandorawear.mobile.infra.storage.SharedPrefsBackendConfigStorage
import com.pandorawear.mobile.infra.storage.SharedPrefsDeviceCredentialsStorage
import com.pandorawear.mobile.models.AlarmActionDto
import com.pandorawear.mobile.models.AlarmDeviceUiModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class WearBridgeService : WearableListenerService() {

    companion object {
        private const val TAG = "WearBridgeService"
        private const val ERROR_BACKEND_UNAVAILABLE = "BACKEND_UNAVAILABLE"
        private const val ERROR_NO_DEVICE = "NO_DEVICE"
        private const val ERROR_COMMAND_FAILED = "COMMAND_FAILED"
        private const val ERROR_INCOMPATIBLE_PROTOCOL = "INCOMPATIBLE_PROTOCOL"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var backendConfigStorage: BackendConfigStorage
    private lateinit var deviceCredentialsStorage: DeviceCredentialsStorage

    @Volatile
    private var backendApiClient: BackendApiClient? = null

    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    private val statusRequestAdapter: JsonAdapter<StatusRequestPayload> by lazy {
        moshi.adapter(StatusRequestPayload::class.java)
    }

    private val statusResponseAdapter: JsonAdapter<StatusResponsePayload> by lazy {
        moshi.adapter(StatusResponsePayload::class.java)
    }

    private val commandRequestAdapter: JsonAdapter<CommandRequestPayload> by lazy {
        moshi.adapter(CommandRequestPayload::class.java)
    }

    private val commandResponseAdapter: JsonAdapter<CommandResponsePayload> by lazy {
        moshi.adapter(CommandResponsePayload::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        backendConfigStorage = SharedPrefsBackendConfigStorage(this)
        deviceCredentialsStorage = SharedPrefsDeviceCredentialsStorage(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.coroutineContext.cancel()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        val nodeId = messageEvent.sourceNodeId
        val path = messageEvent.path
        val dataBytes = messageEvent.data

        scope.launch {
            when (path) {
                WearBridgePaths.STATUS_GET -> handleStatusRequest(nodeId, dataBytes)

                WearBridgePaths.COMMAND -> handleCommandRequest(nodeId, dataBytes)

                else -> {
                    Log.w(TAG, "Unknown path: $path")
                }
            }
        }
    }

    private fun resolveBackendInitError(): String {
        val config = backendConfigStorage.load()
        if (config == null) {
            Log.w(TAG, "Backend config is null while resolving error")
            return ERROR_BACKEND_UNAVAILABLE
        }

        val creds = deviceCredentialsStorage.load()
        if (creds == null) {
            Log.w(TAG, "Device credentials are null while resolving error")
            return ERROR_NO_DEVICE
        }

        return ERROR_BACKEND_UNAVAILABLE
    }

    private fun prepareBackendClient(): BackendApiClient? {
        val config = backendConfigStorage.load() ?: run {
            Log.w(TAG, "Backend config is null")
            backendApiClient = null
            return null
        }

        val credentials = deviceCredentialsStorage.load() ?: run {
            Log.w(TAG, "Device credentials are null")
            backendApiClient = null
            return null
        }

        val existing = backendApiClient
        if (existing != null) {
            return existing
        }

        val baseUrl = BackendUrls.baseUrl(config.host, config.port)
        val client = BackendApiClientFactory.create(
            baseUrl = baseUrl,
            credentialsStorage = deviceCredentialsStorage,
        )
        backendApiClient = client

        return client
    }

    private suspend fun handleStatusRequest(nodeId: String, data: ByteArray) {
        val json = data.decodeToString()

        val request = try {
            statusRequestAdapter.fromJson(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse StatusRequestPayload", e)
            sendStatusError(
                nodeId = nodeId,
                requestId = null,
                error = ERROR_INCOMPATIBLE_PROTOCOL,
            )
            return
        }

        if (request == null) {
            sendStatusError(nodeId, null, ERROR_INCOMPATIBLE_PROTOCOL)
            return
        }


        val backend = prepareBackendClient()
        if (backend == null) {
            val error = resolveBackendInitError()
            sendStatusError(nodeId, request.requestId, error)
            return
        }

        try {
            val devices = backend.getDevices()
            val statusDto = mapDevicesToStatus(devices)
            sendStatusSuccess(nodeId, request.requestId, statusDto)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch devices for status", e)
            sendStatusError(nodeId, request.requestId, ERROR_BACKEND_UNAVAILABLE)
        }
    }


    private fun sendStatusSuccess(
        nodeId: String,
        requestId: String?,
        status: StatusDto,
    ) {
        val payload = StatusResponsePayload(
            requestId = requestId,
            alarmDeviceId = status.alarmDeviceId,
            status = status,
        )

        val json = statusResponseAdapter.toJson(payload)
        sendMessage(nodeId, WearBridgePaths.STATUS_RESPONSE, json)
    }


    private suspend fun handleCommandRequest(nodeId: String, data: ByteArray) {
        val json = data.decodeToString()
        Log.d("WearBridgeService", "payload:${json}")
        val request = try {
            commandRequestAdapter.fromJson(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse CommandRequestPayload", e)
            sendCommandError(
                nodeId = nodeId,
                requestId = null,
            )
            return
        }

        if (request == null) {
            sendCommandError(nodeId, null)
            return
        }

        Log.d("WearBridgeService", "request:${request}")
        val backend = prepareBackendClient()
        if (backend == null) {
            sendCommandError(nodeId, request.requestId)
            return
        }

        try {
            deviceCredentialsStorage.load() ?: run {
                sendCommandError(nodeId, request.requestId)
                return
            }

            val action = when (request.action.lowercase()) {
                "start" -> AlarmActionDto.START
                "stop" -> AlarmActionDto.STOP
                else -> {
                    sendCommandError(nodeId, request.requestId)
                    return
                }
            }


            backend.sendAlarmCommand(
                alarmDeviceId = request.alarmDeviceId,
                action = action,
            )

            val devices = backend.getDevices()
            Log.d("WearBridgeService", "devices:${devices}")
            val statusDto = mapDevicesToStatus(devices)

            sendCommandSuccess(
                nodeId = nodeId,
                requestId = request.requestId,
                status = statusDto,
            )

        } catch (e: CancellationException) {
            Log.d(TAG, "Coroutine cancelled", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed", e)
            sendCommandError(nodeId, request.requestId)
        }
    }

    private fun sendCommandSuccess(
        nodeId: String,
        requestId: String?,
        status: StatusDto,
    ) {
        val payload = CommandResponsePayload(
            alarmDeviceId = status.alarmDeviceId,
            requestId = requestId,
            success = true,
            status = status,
        )

        val json = commandResponseAdapter.toJson(payload)
        sendMessage(nodeId, WearBridgePaths.COMMAND_RESPONSE, json)
    }

    private fun sendStatusError(
        nodeId: String,
        requestId: String?,
        error: String,
    ) {
        val payload = StatusResponsePayload(
            requestId = requestId, status = StatusDto(
                alarmDeviceId = null,
                name = null,
                isReady = false,
                fuelTank = null,
                cabinTemp = null,
                engineTemp = null,
                batteryVoltage = null,
                engineRunning = null,
                lastUpdateMillis = null,
                error = error,
            ), alarmDeviceId = null
        )

        val json = statusResponseAdapter.toJson(payload)
        sendMessage(nodeId, WearBridgePaths.STATUS_RESPONSE, json)
    }


    private fun sendCommandError(
        nodeId: String,
        requestId: String?,
    ) {
        val payload = CommandResponsePayload(
            alarmDeviceId = null,
            success = false,
            requestId = requestId,
            status = null,
        )

        val json = commandResponseAdapter.toJson(payload)
        sendMessage(nodeId, WearBridgePaths.COMMAND_RESPONSE, json)
    }

    private fun sendMessage(
        nodeId: String,
        path: String,
        json: String,
    ) {
        val bytes = json.encodeToByteArray()
        Wearable.getMessageClient(this).sendMessage(nodeId, path, bytes).addOnFailureListener { e ->
            Log.e(TAG, "Failed to send message to node=$nodeId path=$path", e)
        }
    }

    private fun mapDevicesToStatus(devices: List<AlarmDeviceUiModel>): StatusDto {
        val device = devices.firstOrNull() ?: return StatusDto(
            alarmDeviceId = null,
            name = null,
            isReady = false,
            fuelTank = null,
            cabinTemp = null,
            engineTemp = null,
            batteryVoltage = null,
            engineRunning = null,
            lastUpdateMillis = null,
            error = ERROR_NO_DEVICE,
        )

        return StatusDto(
            alarmDeviceId = device.id,
            name = device.name,
            isReady = true,
            fuelTank = device.fuelTank,
            engineTemp = device.engineTemp,
            cabinTemp = device.cabinTemp,
            batteryVoltage = device.batteryVoltage,
            engineRunning = device.engineRpm > 0,
            lastUpdateMillis = null,
            error = null,

            )
    }
}
