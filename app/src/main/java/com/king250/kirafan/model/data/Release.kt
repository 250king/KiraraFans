package com.king250.kirafan.model.data

import com.google.gson.annotations.SerializedName

data class Release(
    @SerializedName("version")
    val version: String,

    @SerializedName("code")
    val code: Int
)
