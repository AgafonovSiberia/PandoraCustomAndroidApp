package com.pandorawear.mobile.infra.network


object BackendUrls {

    fun baseUrl(host: String, port: String): String {
        val trimmedHost = host.trim().removeSuffix("/")

        val withScheme = if (
            trimmedHost.startsWith("http://") ||
            trimmedHost.startsWith("https://")
        ) {
            trimmedHost
        } else {
            "http://$trimmedHost"
        }

        val withPort = if (port.isNotBlank()) {
            "$withScheme:$port"
        } else {
            withScheme
        }

        return if (withPort.endsWith("/")) {
            withPort
        } else {
            "$withPort/"
        }
    }

    fun readyUrl(host: String, port: String): String {
        val base = baseUrl(host, port).removeSuffix("/")
        return "$base/api/ready"
    }
}
