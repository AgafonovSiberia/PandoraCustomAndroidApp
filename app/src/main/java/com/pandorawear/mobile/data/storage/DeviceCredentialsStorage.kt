package com.pandorawear.mobile.data.storage

interface DeviceCredentialsStorage {
    fun save(credentials: DeviceCredentials)

    fun load(): DeviceCredentials?

    fun clear()
}