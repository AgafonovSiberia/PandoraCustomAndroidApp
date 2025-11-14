package com.pandorawear.mobile.data.network

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object BackendApiClientFactory {

    private const val TAG = "BackendApiClientFactory"

    fun create(
        baseUrl: String,
        isDebug: Boolean = true,
    ): BackendApiClient {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())      // <-- ВАЖНО
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)

        Log.i(TAG, "Creating Retrofit client with baseUrl = $normalizedBaseUrl")



        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()

        val service = retrofit.create(BackendApiService::class.java)

        return RetrofitBackendApiClient(service)
    }

    private fun normalizeBaseUrl(raw: String): String {
        var url = raw.trim()

        if (!url.startsWith("http://")) {
            url = "http://$url"
        }

        if (!url.endsWith("/")) {
            url += "/"
        }

        return url
    }
}
