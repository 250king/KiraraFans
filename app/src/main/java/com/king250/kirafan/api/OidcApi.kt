package com.king250.kirafan.api

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.king250.kirafan.BuildConfig
import com.king250.kirafan.Env
import com.king250.kirafan.api.Api.ktor
import com.king250.kirafan.util.SecurityUtil
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OidcApi(private val context: Context) {
    private val serviceConfig = AuthorizationServiceConfiguration(
        Env.AUTHORIZE_URI.toUri(),
        Env.TOKEN_URI.toUri()
    )

    private val uaConnectionBuilder = ConnectionBuilder { uri ->
        DefaultConnectionBuilder.INSTANCE.openConnection(uri).apply {
            setRequestProperty("User-Agent", "KiraraFans/${BuildConfig.VERSION_NAME}")
        }
    }

    private val config = AppAuthConfiguration.Builder()
        .setConnectionBuilder(uaConnectionBuilder)
        .build()

    private val authService: AuthorizationService by lazy {
        AuthorizationService(context, config)
    }

    fun buildAuthorizationRequest(codeVerifier: String): AuthorizationRequest {
        val codeChallenge = SecurityUtil.generateCodeChallenge(codeVerifier)
        return AuthorizationRequest.Builder(
            serviceConfig,
            Env.CLIENT_ID,
            ResponseTypeValues.CODE,
            Env.REDIRECT_URI.toUri()
        )
            .setScope("openid profile offline_access member:all")
            .setCodeVerifier(codeVerifier, codeChallenge, "S256")
            .setPrompt("consent")
            .setAdditionalParameters(
                mapOf(
                    "resource" to Env.RESOURCE_URI,
                )
            )
            .build()
    }

    fun startLogin(codeVerifier: String, completionIntent: PendingIntent, cancelIntent: PendingIntent) {
        val request = buildAuthorizationRequest(codeVerifier)
        authService.performAuthorizationRequest(request, completionIntent, cancelIntent)
    }

    fun handleRedirect(intent: Intent, onSuccess: (TokenResponse) -> Unit, onError: (Throwable) -> Unit) {
        val authEx = AuthorizationException.fromIntent(intent)
        if (authEx != null) {
            onError(authEx)
            return
        }
        val authResponse = AuthorizationResponse.fromIntent(intent)
        if (authResponse == null) {
            onError(IllegalStateException("Missing AuthorizationResponse in redirect intent"))
            return
        }
        val tokenRequest = authResponse.createTokenExchangeRequest()
        authService.performTokenRequest(tokenRequest) { tokenResponse, tokenEx ->
            if (tokenEx != null) {
                onError(tokenEx)
                return@performTokenRequest
            }
            if (tokenResponse == null) {
                onError(IllegalStateException("TokenResponse is null"))
                return@performTokenRequest
            }
            onSuccess(tokenResponse)
        }
    }

    suspend fun refresh(refreshToken: String): TokenResponse {
        val request = TokenRequest.Builder(serviceConfig, Env.CLIENT_ID)
            .setGrantType(GrantTypeValues.REFRESH_TOKEN)
            .setRefreshToken(refreshToken)
            .setAdditionalParameters(
                mapOf(
                    "resource" to Env.RESOURCE_URI
                )
            )
            .build()

        return suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(request) { tokenResponse, tokenEx ->
                if (tokenEx != null) {
                    cont.resumeWithException(tokenEx)
                    return@performTokenRequest
                }
                if (tokenResponse == null) {
                    cont.resumeWithException(IllegalStateException("TokenResponse is null"))
                    return@performTokenRequest
                }
                cont.resume(tokenResponse)
            }
        }
    }

    suspend fun revoke(refreshToken: String) {
        ktor.submitForm(
            url = Env.REVOKE_URI,
            formParameters = parameters {
                append("token", refreshToken)
                append("client_id", Env.CLIENT_ID)
            }
        )
    }
}
