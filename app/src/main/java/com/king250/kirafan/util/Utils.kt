package com.king250.kirafan.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.Settings
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
        .addInterceptor(UserAgentInterceptor)
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    @SuppressLint("HardwareIds")
    fun getDID(contentResolver: ContentResolver): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getUSB(contentResolver: ContentResolver): Boolean {
        return Settings.Secure.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
    }
}