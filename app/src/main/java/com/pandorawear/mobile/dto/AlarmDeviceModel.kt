package com.pandorawear.mobile.dto
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class AlarmDeviceUiModel(
    val id: Integer,
    val name: String,
    val engineTemp: String,
    val cabinTemp: String,
    val outTemp: String,
    val batteryVoltage: Double,
    val isArmed: Boolean = true,
    val engineRpm: Int,
    val fuelTank: Int
)

fun PandoraDeviceDto.toUiModel(): AlarmDeviceUiModel =
    AlarmDeviceUiModel(
        id = id,
        name = name,
        engineTemp = "${data.engineTemp}°",
        cabinTemp = "${data.cabinTemp}°",
        outTemp = "${data.outTemp}°",
        batteryVoltage = data.voltage,
        engineRpm = data.engineRpm,
        fuelTank = data.fuelTank
    )

@JsonClass(generateAdapter = true)
data class PandoraDeviceDataDto(
    @param:Json(name = "fuel") val fuelTank: Int,
    @param:Json(name = "voltage") val voltage: Double,
    @param:Json(name = "engine_temp") val engineTemp: Int,
    @param:Json(name = "out_temp") val outTemp: Int,
    @param:Json(name = "cabin_temp") val cabinTemp: Int,
    @param:Json(name = "engine_rpm") val engineRpm: Int,
    @param:Json(name = "x") val x: Double,
    @param:Json(name = "y") val y: Double,
)

@JsonClass(generateAdapter = true)
data class PandoraDeviceDto(
    @param:Json(name = "id") val id: Integer,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "model") val model: String,
    @param:Json(name = "data") val data: PandoraDeviceDataDto,
)