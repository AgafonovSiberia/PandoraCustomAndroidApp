package com.pandorawear.mobile.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object BackendHealthChecker {

    private val TAG = "BackendHealthChecker"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .build()

    suspend fun isBackendReady(host: String, port: String): Boolean =
        withContext(Dispatchers.IO) {

            val url = BackendUrls.readyUrl(host, port)

            Log.i(TAG, "🟦 Checking backend health at URL: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            try {
                client.newCall(request).execute().use { response ->

                    Log.i(TAG, "🟧 Response code: ${response.code}")
                    Log.i(TAG, "🟧 Response message: ${response.message}")

                    val body = response.body?.string()
                    Log.i(TAG, "🟩 Response body: $body")

                    if (!response.isSuccessful) {
                        Log.e(TAG, "❌ Unsuccessful response")
                        return@withContext false
                    }

                    val ok = body?.contains("\"status\"") == true &&
                            body.contains("\"ok\"")

                    Log.i(TAG, "🟩 Backend ready: $ok")
                    return@withContext ok
                }
            } catch (e: IOException) {
                Log.e(TAG, "❌ IOException while checking backend readiness", e)
                return@withContext false
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error during health-check", e)
                return@withContext false
            }
        }
}
