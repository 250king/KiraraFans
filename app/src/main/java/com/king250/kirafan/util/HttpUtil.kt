package com.king250.kirafan.util

import com.king250.kirafan.Application
import com.king250.kirafan.interceptor.IdentifyInterceptor
import com.king250.kirafan.interceptor.TokenInterceptor
import com.king250.kirafan.interceptor.UserAgentInterceptor
import com.king250.kirafan.service.ProtectedApiService
import com.king250.kirafan.service.AuthService
import com.king250.kirafan.service.PublicApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HttpUtil {
    private val okHttpInstance = OkHttpClient.Builder().addInterceptor(UserAgentInterceptor())

    private val retrofitInstance = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())

    val auth: AuthService = retrofitInstance.baseUrl("https://account.250king.top/application/o/")
        .apply {
            val client = okHttpInstance.addInterceptor(IdentifyInterceptor()).build()
            client(client)
        }
        .build()
        .create(AuthService::class.java)

    val public: PublicApiService = retrofitInstance.baseUrl("https://kirafan.xyz")
        .apply {
            val client = okHttpInstance.build()
            client(client)
        }
        .build()
        .create(PublicApiService::class.java)

    val protected: ProtectedApiService = retrofitInstance.baseUrl("https://api.kirafan.xyz")
        .apply {
            val client = okHttpInstance.addInterceptor(
                TokenInterceptor(Application.application)
            ).build()
            client(client)
        }
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ProtectedApiService::class.java)
}
