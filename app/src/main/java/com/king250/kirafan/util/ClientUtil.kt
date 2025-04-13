package com.king250.kirafan.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import java.io.File

object ClientUtil {
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
            val result = Class.forName("android.os.SystemProperties")
                .getMethod("get", String.Companion::class.java)
                .invoke(null, "init.svc.adbd") as String
            return result == "running"
        }
        catch (_: Exception) {
            return Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
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