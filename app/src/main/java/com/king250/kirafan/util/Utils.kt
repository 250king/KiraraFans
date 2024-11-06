package com.king250.kirafan.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.JsonParser
import com.king250.kirafan.BuildConfig
import com.king250.kirafan.Env
import com.king250.kirafan.dataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.Date

object UserAgentInterceptor: Interceptor {
    override fun intercept(chain: Chain): Response {
        val request = chain
            .request()
            .newBuilder()
            .header("User-Agent", "KiraraFan/${BuildConfig.VERSION_NAME}")
            .build()
        return chain.proceed(request)
    }
}

class TokenInterceptor(private val context: Context): Interceptor {
    override fun intercept(chain: Chain): Response {
        var accessToken = runBlocking {
            val preferences = context.dataStore.data.firstOrNull()
            preferences?.get(stringPreferencesKey("access_token")) ?: ""
        }
        val parts = accessToken.split(".")
        val payload = JsonParser.parseString(String(Base64.decode(parts[1], Base64.NO_WRAP))).asJsonObject
        val expire = payload.get("exp").asLong
        if (Date().time / 1000 > expire) {
            val refreshToken = runBlocking {
                val preferences = context.dataStore.data.firstOrNull()
                preferences?.get(stringPreferencesKey("refresh_token")) ?: ""
            }
            val body = FormBody
                .Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build()
            val request = Request
                .Builder()
                .url("${Env.SERVER_API}/oauth2/token")
                .header("Authorization", "Basic ${Env.BASIC_AUTH}")
                .post(body)
                .build()
            val response = Utils.http().newCall(request).execute()
            if (response.code == 200) {
                val data = JsonParser.parseString(response.body?.string() ?: "").asJsonObject
                accessToken = data.get("access_token").asString
                runBlocking {
                    context.dataStore.edit {
                        it[stringPreferencesKey("access_token")] = data.get("access_token").asString
                        it[stringPreferencesKey("refresh_token")] = data.get("refresh_token").asString
                    }
                }
            }
            else {
                runBlocking {
                    context.dataStore.edit {
                        it.remove(stringPreferencesKey("access_token"))
                        it.remove(stringPreferencesKey("refresh_token"))
                    }
                }
            }
            response.close()
        }
        val request = chain
            .request()
            .newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(request)
    }
}

object Utils {
    fun http(context: Context? = null): OkHttpClient {
        return OkHttpClient
            .Builder()
            .apply {
                if (context != null) {
                    addInterceptor(TokenInterceptor(context))
                }
            }
            .addInterceptor(UserAgentInterceptor)
            .build()
    }

    @SuppressLint("HardwareIds")
    fun getAndroidID(contentResolver: ContentResolver): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun checkUSB(contentResolver: ContentResolver): Boolean {
        return Settings.Secure.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
    }
}