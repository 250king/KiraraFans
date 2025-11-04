package com.king250.kirafan.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.scottyab.rootbeer.RootBeer
import androidx.core.net.toUri

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
        intent.launchUrl(context, url.toUri())
    }

    fun toast(context: Context, text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }

    fun isDebug(contentResolver: ContentResolver): Boolean {
        return try {
            Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED) == 1
        } catch (_: Settings.SettingNotFoundException) {
            false
        }
    }

    fun isRooted(context: Context): Boolean {
        val rootBeer = RootBeer(context)
        return rootBeer.isRooted
    }
}