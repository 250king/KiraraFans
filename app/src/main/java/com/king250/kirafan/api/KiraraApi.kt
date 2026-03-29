package com.king250.kirafan.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.king250.kirafan.Env
import com.king250.kirafan.api.Api.ktor
import com.king250.kirafan.dataStore
import com.king250.kirafan.model.data.Encrypted
import com.king250.kirafan.model.data.Endpoint
import com.king250.kirafan.model.data.Items
import com.king250.kirafan.model.data.Session
import com.king250.kirafan.model.data.Version
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType
import kotlinx.coroutines.flow.firstOrNull

class KiraraApi(private val context: Context) {
    val storage: DataStore<Preferences> = context.dataStore

    val client = ktor.config {
        defaultRequest {
            url("${Env.RESOURCE_URI}/v2.1/")
        }
        install(Auth) {
            bearer {
                loadTokens {
                    loadToken()
                }
                refreshTokens {
                    refreshToken()
                }
            }
        }
    }

    private suspend fun loadToken(): BearerTokens? {
        val expiresAt = context.dataStore.data.firstOrNull()
            ?.get(longPreferencesKey("expires_at"))
            ?: 0
        if (expiresAt < System.currentTimeMillis()) {
            return refreshToken()
        }
        val accessToken = context.dataStore.data.firstOrNull()
            ?.get(stringPreferencesKey("access_token"))
            ?: return null
        val refreshToken = context.dataStore.data.firstOrNull()
            ?.get(stringPreferencesKey("refresh_token"))
            ?: return null
        return BearerTokens(accessToken, refreshToken)
    }

    private suspend fun refreshToken(): BearerTokens? {
        val refreshToken = context.dataStore.data.firstOrNull()
            ?.get(stringPreferencesKey("refresh_token"))
            ?: return null
        val token = try {
            Api.oidc.refresh(refreshToken)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        if (token.accessToken == null || token.refreshToken == null) {
            return null
        }
        storage.edit {
            token.accessToken?.let { v ->
                it[stringPreferencesKey("access_token")] = v
            }
            token.refreshToken?.let { v ->
                it[stringPreferencesKey("refresh_token")] = v
            }
            token.idToken?.let { v ->
                it[stringPreferencesKey("id_token")] = v
            }
            token.accessTokenExpirationTime?.let { v ->
                it[longPreferencesKey("expires_at")] = v
            }
        }
        return BearerTokens(token.accessToken!!, token.refreshToken)
    }

    suspend fun getEndpoints(): Items<Endpoint> {
        return client.get("endpoints").body()
    }

    suspend fun createSession(data: Session): Encrypted {
        return client.post("session") {
            contentType(Application.Json)
            setBody(data)
        }.body()
    }

    suspend fun revokeSession() {
        client.delete("session")
    }

    suspend fun getVersion(): Version {
        return client.get("version").body()
    }

    suspend fun getArticle(name: String): String {
        return client.get("articles/$name").body()
    }
}