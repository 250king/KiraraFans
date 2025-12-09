package com.king250.kirafan.api

import com.king250.kirafan.model.data.Release
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface PublicService {
    @GET("version")
    fun getRelease(): Call<Release>

    @GET("article/terms")
    fun getTerms(): Call<ResponseBody>

    @GET("article/help")
    fun getHelp(): Call<ResponseBody>
}
