package com.king250.kirafan.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.king250.kirafan.Env
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

object SecurityUtil {
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

    fun initKeyStore(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(Env.KEY_ALIAS)
            if (!keyStore.containsAlias(Env.KEY_ALIAS)) {
                val generator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA,
                    "AndroidKeyStore"
                )
                val builder = KeyGenParameterSpec.Builder(
                    Env.KEY_ALIAS,
                    KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeySize(2048)
                    .setDigests(KeyProperties.DIGEST_SHA1)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    .build()
                generator.initialize(builder)
                generator.generateKeyPair()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getPublicKey(): String? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val certificate = keyStore.getCertificate(Env.KEY_ALIAS) ?: return null
            val base64Key = Base64.encodeToString(certificate.publicKey.encoded, Base64.NO_WRAP)
            "-----BEGIN PUBLIC KEY-----\n$base64Key\n-----END PUBLIC KEY-----"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decrypt(aesBase64: String, ivBase64: String, contentBase64: String): String? {
        return try {
            val aes = Base64.decode(aesBase64, Base64.DEFAULT)
            val iv = Base64.decode(ivBase64, Base64.DEFAULT)
            val content = Base64.decode(contentBase64, Base64.DEFAULT)
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val private = keyStore.getEntry(Env.KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
            var cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
            val params = OAEPParameterSpec(
                "SHA-1",
                "MGF1",
                MGF1ParameterSpec.SHA1,
                PSource.PSpecified.DEFAULT
            )
            cipher.init(Cipher.DECRYPT_MODE, private.privateKey, params)
            val key = SecretKeySpec(cipher.doFinal(aes), "AES")
            val spec = GCMParameterSpec(128, iv)
            cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            String(cipher.doFinal(content))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}