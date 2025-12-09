package com.king250.kirafan.model.data

data class Encrypted(
    val key: String,
    val iv: String,
    val data: String
)
