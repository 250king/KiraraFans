package com.king250.kirafan

import android.os.Build

object Env {
    val HEIGHT_ANDROID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val TARGET_PACKAGE = if (HEIGHT_ANDROID) {"com.vmos.openapp"} else {"com.aniplex.kirarafantasia"}

    val DEVICE_ABI: String? = Build.SUPPORTED_ABIS[0]

    const val SERVER_API = "https://api.250king.top"

    const val KIRARA_API = "https://vpc.sparklefantasia.com/api"

    const val RELEASE_API = "https://api.github.com/repos/gd1000m/Kirara-Repo/releases/latest"

    const val AUTHORIZE_URI = "https://auth.250king.top/auth/authorize"

    const val REDIRECT_URI = "kirara://callback/login"

    const val CLIENT_ID = "2e97fea0-78ee-404c-8d32-1639bd79596d"

    const val BASIC_AUTH = "MmU5N2ZlYTAtNzhlZS00MDRjLThkMzItMTYzOWJkNzk1OTZkOlQ3OSFDKXMkcThoKmNQZWEwI0gzbnlEV0tGelhCWWNa"

    const val PROXY_HOST = "vpc.sparklefantasia.com:443"

    const val SERVICE_CHANNEL = "com.king250.kirafan.service.ConnectorVpnService"

    const val NOTIFICATION_CHANNEL = "com.king.kirafan.Notification"

    const val UI_CHANNEL = "com.king250.kirafan.ui.activity.MainActivity"

    const val SERVICE_STARTED = 0

    const val SERVICE_STOPPED = 1

    const val STOP_SERVICE = 2

    const val START_FAILED = 3

    const val COPY_URL = 4
}
