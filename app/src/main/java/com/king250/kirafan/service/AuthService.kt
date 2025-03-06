package com.king250.kirafan.service

import com.king250.kirafan.Env
import com.king250.kirafan.model.data.Token
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthService {
    @FormUrlEncoded
    @POST("token/")
    fun login(
        @Field("redirect_uri") redirectUri: String = Env.REDIRECT_URI,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String,
    ) : Call<Token>

    @FormUrlEncoded
    @POST("token/")
    fun refresh(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
    ) : Call<Token>

    @FormUrlEncoded
    @POST("revoke/")
    fun logout(@Field("token") token: String) : Call<Unit>
}