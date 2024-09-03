package com.king250.kirafan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserItem (
    var name: String = "",
    var avatar: String = "",
    var token: String = "",
    var expire: Long = 0L
) : Parcelable
