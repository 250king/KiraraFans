package com.king250.kirafan.service

import com.king250.kirafan.Env
import com.king250.kirafan.model.data.Release
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface PublicApiService {
    @GET
    fun getRelease(@Url url: String = Env.RELEASE_API): Call<Release>
}
