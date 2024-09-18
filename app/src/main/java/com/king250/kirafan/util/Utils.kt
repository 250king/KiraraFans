package com.king250.kirafan.util

import android.provider.Settings
import android.util.Base64
import com.king250.kirafan.BuildConfig
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit

object UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Chain): Response {
        val request = chain
            .request()
            .newBuilder()
            .header("User-Agent", "KiraraFan/${BuildConfig.VERSION_NAME}")
            .build()
        return chain.proceed(request)
    }
}

object Utils {
    val httpClient = OkHttpClient
        .Builder()
        .readTimeout(1, TimeUnit.DAYS)
        .connectTimeout(1, TimeUnit.DAYS)
        .addInterceptor(UserAgentInterceptor)
        .build()

    fun getDID(): String {
        val androidId = Settings.Secure.ANDROID_ID.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(androidId.copyOf(32), Base64.NO_PADDING.or(Base64.URL_SAFE))
    }
}