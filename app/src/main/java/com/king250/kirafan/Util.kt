package com.king250.kirafan

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.dnsoverhttps.DnsOverHttps
import java.io.File
import java.util.Date
import java.util.concurrent.TimeUnit

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
            val response = Util.http(context).newCall(request).execute()
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

object Util {
    fun http(context: Context, withCredential: Boolean = false): OkHttpClient {
        val doh = runBlocking {
            val preferences = context.dataStore.data.firstOrNull()
            preferences?.get(booleanPreferencesKey("dns")) ?: false
        }
        val client = OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor)
            .apply {
                if (withCredential) {
                    addInterceptor(TokenInterceptor(context))
                }
            }
        if (doh) {
            val cache = Cache(File(context.cacheDir, "okhttp"), 10 * 1024 * 1024)
            val bootstrap = client.cache(cache).build()
            val dns = DnsOverHttps
                .Builder()
                .client(bootstrap)
                .url("https://doh.apad.pro/dns-query".toHttpUrl())
                .build()
            client.dns(dns)
        }
        return client.build()
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId(contentResolver: ContentResolver): String {
        return Base64.encodeToString(
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID).toByteArray().copyOf(32),
            Base64.NO_PADDING.or(Base64.URL_SAFE)
        )
    }

    fun open(context: Context, url: String) {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, Uri.parse(url))
    }

    fun toast(context: Context, text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }

    @SuppressLint("PrivateApi")
    fun isDebug(contentResolver: ContentResolver): Boolean {
        try {
            val result = Class
                .forName("android.os.SystemProperties")
                .getMethod("get", String.Companion::class.java)
                .invoke(null, "init.svc.adbd") as String
            return result == "running"
        }
        catch (_: Exception) {
            return Settings.Secure.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        }
    }

    fun isRooted(): Boolean {
        val paths = listOf(
            "/system/xbin/su",
            "/system/bin/su",
            "/system/su",
            "/sbin/su",
            "/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    /*
    该部分暂时有问题停用
    fun isEmulator(): Boolean {
        return File("/system/lib/libhoudini.so").exists()
    }
    */
}