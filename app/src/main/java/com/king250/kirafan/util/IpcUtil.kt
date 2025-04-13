package com.king250.kirafan.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.king250.kirafan.Env

object IpcUtil {
    fun toService(context: Context, message: Int) {
        val intent = Intent(Env.SERVICE_CHANNEL)
        intent.putExtra("action", message)
        intent.`package` = context.packageName
        context.sendBroadcast(intent)
    }

    fun toUI(context: Context, message: Int) {
        val intent = Intent(Env.UI_CHANNEL)
        intent.putExtra("action", message)
        intent.`package` = context.packageName
        Log.i("broadcaster", "toUI: $message")
        context.sendBroadcast(intent)
    }
}