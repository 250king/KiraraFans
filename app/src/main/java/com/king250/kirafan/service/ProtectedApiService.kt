package com.king250.kirafan.service

import com.king250.kirafan.model.data.ChangeOperation
import com.king250.kirafan.model.data.Endpoint
import com.king250.kirafan.model.data.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ProtectedApiService {
    @GET("terms")
    fun getTerms(): Call<ResponseBody>

    @GET("endpoint")
    fun getEndpoints() : Call<List<Endpoint>>

    @POST("session")
    fun changeEndpoint(@Body payload: ChangeOperation) : Call<ResponseBody>

    @DELETE("session")
    fun revokeSession() : Call<Unit>

    @GET
    fun getProfile(@Url url: String = "https://account.250king.top/application/o/userinfo/") : Call<User>
}
