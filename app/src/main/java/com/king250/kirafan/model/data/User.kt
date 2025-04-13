package com.king250.kirafan.model.data

import com.google.gson.annotations.SerializedName
import java.util.*

data class User(
    @SerializedName("sub")
    val id: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("avatar")
    val avatar: String,

    @SerializedName("groups")
    val groups: List<String>,

    @SerializedName("is_enabled")
    val isEnabled: Boolean,

    @SerializedName("created_at")
    val createdAt: Date
)
