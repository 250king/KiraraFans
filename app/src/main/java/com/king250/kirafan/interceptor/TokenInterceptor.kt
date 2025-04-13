package com.king250.kirafan.interceptor

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.king250.kirafan.dataStore
import com.king250.kirafan.util.HttpUtil
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

class TokenInterceptor(private val context: Context): Interceptor {
    override fun intercept(chain: Chain): Response {
        var accessToken = runBlocking {
            val preferences = context.dataStore.data.firstOrNull()
            preferences?.get(stringPreferencesKey("access_token")) ?: ""
        }
        val expiresIn = runBlocking {
            val preferences = context.dataStore.data.firstOrNull()
            preferences?.get(longPreferencesKey("expires_in")) ?: 0
        }
        if (System.currentTimeMillis() / 1000 > expiresIn) {
            val refreshToken = runBlocking {
                val preferences = context.dataStore.data.firstOrNull()
                preferences?.get(stringPreferencesKey("refresh_token")) ?: ""
            }
            val response = HttpUtil.auth.refresh(refreshToken = refreshToken).execute()
            val token = response.body()
            if (response.isSuccessful && token != null) {
                accessToken = token.accessToken
                runBlocking {
                    context.dataStore.edit {
                        it[stringPreferencesKey("access_token")] = token.accessToken
                        it[stringPreferencesKey("refresh_token")] = token.refreshToken
                        it[longPreferencesKey("expires_in")] = System.currentTimeMillis() / 1000 + token.expiresIn
                    }
                }
            }
            else {
                runBlocking {
                    context.dataStore.edit {
                        it.remove(booleanPreferencesKey("agreed"))
                        it.remove(stringPreferencesKey("access_token"))
                        it.remove(stringPreferencesKey("refresh_token"))
                        it.remove(longPreferencesKey("expires_in"))
                    }
                }
            }
        }
        val request = chain
            .request()
            .newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(request)
    }
}
