package com.king250.kirafan.api

import com.king250.kirafan.model.data.ChangeOperation
import com.king250.kirafan.model.data.Endpoint
import com.king250.kirafan.model.data.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ProtectedService {
    @GET("article/terms")
    fun getTerms(): Call<ResponseBody>

    @GET("article/help")
    fun getHelp(): Call<ResponseBody>

    @GET("endpoint")
    fun getEndpoints() : Call<List<Endpoint>>

    @POST("session")
    fun changeEndpoint(@Body payload: ChangeOperation) : Call<ResponseBody>

    @DELETE("session")
    fun revokeSession() : Call<Unit>

    @HEAD("session")
    fun keepSession() : Call<Unit>

    @GET
    fun getProfile(@Url url: String = "https://account.250king.top/application/o/userinfo/") : Call<User>
}
