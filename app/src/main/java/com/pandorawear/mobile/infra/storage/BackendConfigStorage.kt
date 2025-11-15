package com.pandorawear.mobile.infra.storage

interface BackendConfigStorage {

    fun save(config: BackendConfig)

    fun load(): BackendConfig?

    fun clear()
}

