package com.king250.kirafan.api

import android.content.Context
import com.king250.kirafan.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.gson.gson

object Api {
    private lateinit var appContext: Context

    val kirara: KiraraApi by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        KiraraApi(appContext)
    }

    val oidc: OidcApi by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        OidcApi(appContext)
    }

    val ktor = HttpClient {
        defaultRequest {
            headers {
                set("User-Agent", "KiraraFans/${BuildConfig.VERSION_NAME}")
            }
        }
        install(ContentNegotiation) {
            gson()
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}