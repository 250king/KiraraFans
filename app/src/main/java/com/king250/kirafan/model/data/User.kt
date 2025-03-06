package com.king250.kirafan.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
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
) : Parcelable
