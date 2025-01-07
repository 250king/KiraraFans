package com.king250.kirafan.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.king250.kirafan.dataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.io.File
import java.util.concurrent.TimeUnit

object ClientUtil {
    fun http(context: Context, withCredential: Boolean = false): OkHttpClient {
        val doh = runBlocking {
            val preferences = context.dataStore.data.firstOrNull()
            preferences?.get(booleanPreferencesKey("dns")) == true
        }
        val client = OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor())
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
}