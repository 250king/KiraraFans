package com.king250.kirafan.api

import com.king250.kirafan.Env
import com.king250.kirafan.model.data.Release
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface PublicService {
    @GET
    fun getRelease(@Url url: String = Env.RELEASE_API): Call<Release>
}
