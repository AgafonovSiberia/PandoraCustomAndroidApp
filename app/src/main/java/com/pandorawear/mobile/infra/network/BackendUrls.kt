package com.pandorawear.mobile.infra.network



object BackendUrls {

    fun baseUrl(host: String, port: String): String {
        var url = host.trim()

        if (!url.endsWith("/")) {
            url = "$url/"
        }

        return url
    }

    fun readyUrl(host: String, port: String): String {
        val base = baseUrl(host, port).removeSuffix("/")
        return "$base/api/ready"
    }
}
