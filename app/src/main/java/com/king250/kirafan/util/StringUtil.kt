package com.king250.kirafan.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object StringUtil {
    fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val code = ByteArray(64)
        secureRandom.nextBytes(code)
        return Base64.encodeToString(code, Base64.URL_SAFE or Base64.URL_SAFE or Base64.NO_PADDING)
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hash = sha256.digest(codeVerifier.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }
}