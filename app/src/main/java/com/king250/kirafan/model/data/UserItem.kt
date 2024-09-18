package com.king250.kirafan.model.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserItem (
    var name: String = "",
    var avatar: String = ""
) : Parcelable
