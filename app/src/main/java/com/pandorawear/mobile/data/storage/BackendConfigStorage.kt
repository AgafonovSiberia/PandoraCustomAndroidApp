package com.pandorawear.mobile.data.storage

interface BackendConfigStorage {

    fun save(config: BackendConfig)

    fun load(): BackendConfig?

    fun clear()
}

