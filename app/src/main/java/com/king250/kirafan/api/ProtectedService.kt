package com.king250.kirafan.api

import com.king250.kirafan.model.data.Encrypted
import com.king250.kirafan.model.data.Session
import com.king250.kirafan.model.data.Endpoint
import com.king250.kirafan.model.data.Items
import com.king250.kirafan.model.data.User
import retrofit2.Call
import retrofit2.http.*

interface ProtectedService {
    @GET("endpoint")
    fun getEndpoints() : Call<Items<Endpoint>>

    @POST("session")
    fun createSession(@Body payload: Session) : Call<Encrypted>

    @DELETE("session")
    fun revokeSession() : Call<Unit>

    @GET
    fun getProfile(@Url url: String = "https://account.250king.top/application/o/userinfo/") : Call<User>
}
