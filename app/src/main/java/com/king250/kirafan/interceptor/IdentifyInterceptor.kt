package com.king250.kirafan.interceptor

import android.util.Base64
import com.king250.kirafan.Env
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

class IdentifyInterceptor: Interceptor {
    override fun intercept(chain: Chain): Response {
        val auth = "${Env.CLIENT_ID}:".toByteArray()
        val request = chain.request()
            .newBuilder()
            .header("Authorization", "Basic ${Base64.encodeToString(auth, Base64.NO_WRAP)}")
            .build()
        return chain.proceed(request)
    }
}
