package com.king250.kirafan

import android.os.Build

object Env {
    val HEIGHT_ANDROID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val TARGET_PACKAGE = if (HEIGHT_ANDROID) {"com.vmos.openapp"} else {"com.aniplex.kirarafantasia"}

    val DEVICE_ABI: String = Build.SUPPORTED_ABIS[0]

    const val AUTHORIZE_URI = "https://account.250king.top/application/o/authorize/"

    const val REDIRECT_URI = "kirara://callback/login"

    const val CLIENT_ID = "wimwBO8uyk6DODegsIbdMMtm88baBwYqZkyyOVPO"

    const val RELEASE_API = "https://api.github.com/repos/gd1000m/Kirara-Repo/releases/latest"

    //const val ENDPOINT_LIST = "https://kirafan.xyz/.well-known/endpoints.json"

    const val PROXY_HOST = "hk.tunnel.kirafan.xyz:443"

    const val SERVICE_CHANNEL = "com.king250.kirafan.service.ConnectorVpnService"

    const val NOTIFICATION_CHANNEL = "com.king.kirafan.Notification"

    const val UI_CHANNEL = "com.king250.kirafan.ui.activity.MainActivity"

    const val SERVICE_STARTED = 0

    const val SERVICE_STOPPED = 1

    const val STOP_SERVICE = 2

    const val START_FAILED = 3
}
