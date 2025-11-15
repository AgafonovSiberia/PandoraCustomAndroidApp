package com.pandorawear.mobile.infra.storage

interface DeviceCredentialsStorage {
    fun save(credentials: DeviceCredentials)

    fun load(): DeviceCredentials?

    fun clear()
}