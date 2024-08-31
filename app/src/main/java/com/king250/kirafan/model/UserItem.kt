package com.king250.kirafan.model

import java.security.Timestamp
import java.time.Instant
import java.time.LocalDateTime

data class UserItem (
    var name: String = "",
    var avatar: String = "",
    var token: String = "",
    var expire: Long = 0L
)