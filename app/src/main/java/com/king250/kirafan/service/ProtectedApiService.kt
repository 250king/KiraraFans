package com.king250.kirafan.service

import com.king250.kirafan.model.data.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface ProtectedApiService {
    @POST("/proxy/session")
    fun initProxy() : Call<ResponseBody>

    @DELETE("/proxy/session")
    fun revokeProxy() : Call<Unit>

    @GET
    fun getProfile(@Url url: String = "https://account.250king.top/application/o/userinfo/") : Call<User>
}
