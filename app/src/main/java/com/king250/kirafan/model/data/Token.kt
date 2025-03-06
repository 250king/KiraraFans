package com.king250.kirafan.model.data

import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("expires_in")
    val expiresIn: Int
)
