package com.king250.kirafan

import android.os.Build

object Env {
    val HEIGHT_ANDROID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val DEVICE_ABI: String = Build.SUPPORTED_ABIS[0]

    val TARGET_PACKAGE = if (HEIGHT_ANDROID) {
        "com.vmos.openapp"
    }
    else {
        "com.aniplex.kirarafantasia"
    }

    const val RESOURCE_URI = "https://api.kirafan.site"

    const val TOKEN_URI = "https://auth.250king.top/oidc/token"

    const val AUTHORIZE_URI = "https://auth.250king.top/oidc/auth"

    const val REDIRECT_URI = "kirara://callback/login"

    const val REVOKE_URI = "https://auth.250king.top/oidc/token/revocation"

    const val CLIENT_ID = "6obhroec5x8qaho2gudmp"

    const val SERVICE_CHANNEL = "com.king250.kirafan.service.ConnectorVpnService"

    const val NOTIFICATION_CHANNEL = "com.king.kirafan.Notification"

    const val UI_CHANNEL = "com.king250.kirafan.ui.activity.MainActivity"

    const val KEY_ALIAS = "com.king250.kirafan.ui.activity.ConfigKey"

    const val OIDC_COMPLETE = "com.king250.kirafan.oidc.complete"

    const val OIDC_CANCEL = "com.king250.kirafan.oidc.cancel"

    const val SERVICE_STARTED = 0

    const val SERVICE_STOPPED = 1

    const val STOP_SERVICE = 2
}
