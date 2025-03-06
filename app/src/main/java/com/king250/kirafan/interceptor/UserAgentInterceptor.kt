package com.king250.kirafan.interceptor

import com.king250.kirafan.BuildConfig
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

class UserAgentInterceptor: Interceptor {
    override fun intercept(chain: Chain): Response {
        val request = chain
            .request()
            .newBuilder()
            .header("User-Agent", "KiraraFan/${BuildConfig.VERSION_NAME}")
            .build()
        return chain.proceed(request)
    }
}
