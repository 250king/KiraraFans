package com.king250.kirafan.handler

import android.app.Service

interface ServiceHandler {
    fun getService(): Service

    fun startService()

    fun stopService()

    fun vpnProtect(socket: Int): Boolean
}