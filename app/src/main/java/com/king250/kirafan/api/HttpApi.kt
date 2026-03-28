package com.king250.kirafan.api

import android.content.Context
import com.king250.kirafan.interceptor.IdentifyInterceptor
import com.king250.kirafan.interceptor.TokenInterceptor
import com.king250.kirafan.interceptor.UserAgentInterceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object HttpApi {
    lateinit var oauth: AuthService

    lateinit var public: PublicService

    lateinit var protected: ProtectedService

    fun init(context: Context) {
        val okHttpInstance = OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor())
            .readTimeout(1, TimeUnit.MINUTES)
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
        val retrofitInstance = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
        oauth = retrofitInstance.baseUrl("https://account.250king.top/application/o/")
            .client(okHttpInstance.addInterceptor(IdentifyInterceptor()).build()).build()
            .create(AuthService::class.java)
        public = retrofitInstance.baseUrl("https://api.kirafan.site/v2.0/")
            .client(okHttpInstance.build()).build()
            .create(PublicService::class.java)
        protected = retrofitInstance.baseUrl("https://api.kirafan.site/v2.0/")
            .client(okHttpInstance.addInterceptor(TokenInterceptor(context)).build()).build()
            .create(ProtectedService::class.java)
    }
}