package com.king250.kirafan.model.data

import com.google.gson.annotations.SerializedName

data class Release(
    @SerializedName("tag_name")
    val version: String
)
