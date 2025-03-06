package com.king250.kirafan.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.king250.kirafan.Env
import com.king250.kirafan.dataStore
import com.king250.kirafan.handler.ConnectorHandler
import com.king250.kirafan.model.data.Token
import com.king250.kirafan.model.view.MainView
import com.king250.kirafan.ui.page.AbiWarning
import com.king250.kirafan.ui.page.HomePage
import com.king250.kirafan.ui.theme.KiraraFansTheme
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.util.HttpUtil
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var compatSplashScreen: SplashScreen

    private lateinit var vpnPermissionActivity: ActivityResultLauncher<Intent>

    private lateinit var notificationSettingActivity: ActivityResultLauncher<Intent>

    lateinit var permissionActivity: ActivityResultLauncher<String>

    lateinit var apkAbi: String

    var challenge: String? = null

    val v: MainView by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        compatSplashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        apkAbi = when (File(applicationInfo.nativeLibraryDir).name) {
            "arm64" -> "arm64-v8a"
            "arm" -> "armeabi-v7a"
            else -> File(applicationInfo.nativeLibraryDir).name
        }
        if (Env.DEVICE_ABI == apkAbi) {
            vpnPermissionActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    ConnectorHandler.startV2Ray(this)
                }
                else {
                    v.showSnackBar("这个是程序运行要用到的，所以还是求求你授权吧~")
                }
            }
            notificationSettingActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                        v.showSnackBar("这个是程序运行要用到的，所以还是求求你授权吧~")
                    }
                    else {
                        connect()
                    }
                }
                else {
                    val enabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
                    if (!enabled) {
                        v.showSnackBar("这个是程序运行要用到的，所以还是求求你授权吧~")
                    }
                    else {
                        connect()
                    }
                }
            }
            permissionActivity = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    connect()
                }
                else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                        v.showSnackBar("这个是程序运行要用到的，所以还是求求你授权吧~")
                    }
                    else{
                        ClientUtil.toast(this, "由于你已经设置不允许再请求权限了，所以只能你自己设置了（")
                        enableNotification()
                    }
                }
            }
            onBackPressedDispatcher.addCallback(this) {
                moveTaskToBack(false)
            }
            setContent {
                KiraraFansTheme {
                    HomePage(this)
                }
            }
            lifecycleScope.launch {
                v.init()
                val token = dataStore.data.map{it[stringPreferencesKey("access_token")]}.firstOrNull()
                compatSplashScreen.setKeepOnScreenCondition {false}
                if (token == null) {
                    v.setIsLoading(false)
                }
                else {
                    v.refresh()
                }
                v.check()
            }
        }
        else {
            setContent {
                KiraraFansTheme {
                    AbiWarning(this)
                }
            }
            compatSplashScreen.setKeepOnScreenCondition{false}
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val info = packageManager.getPackageInfo(Env.TARGET_PACKAGE, 0)
            v.setVersion(if (Env.HEIGHT_ANDROID) {"VMOS ${info.versionName}"} else {info.versionName})
        }
        catch (_: Exception) {
            v.setVersion(null)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.data == null && intent.action != Intent.ACTION_VIEW) {
            v.setIsLoading(false)
            return
        }
        v.setIsLoading(true)
        HttpUtil.authApi.login(
            code = intent.data!!.getQueryParameter("code") ?: "",
            codeVerifier = challenge ?: ""
        ).enqueue(object : Callback<Token> {
            override fun onResponse(p0: Call<Token>, p1: Response<Token>) {
                if (!p1.isSuccessful) {
                    v.setIsLoading(false)
                    return
                }
                val token = p1.body()
                if (token == null) {
                    v.setIsLoading(false)
                    return
                }
                lifecycleScope.launch {
                    dataStore.edit {
                        it[stringPreferencesKey("access_token")] = token.accessToken
                        it[stringPreferencesKey("refresh_token")] = token.refreshToken
                        it[longPreferencesKey("expires_in")] = System.currentTimeMillis() / 1000 + token.expiresIn
                    }
                    v.refresh()
                }
            }

            override fun onFailure(p0: Call<Token>, p1: Throwable) {
                p1.printStackTrace()
                v.setIsLoading(false)
                v.showSnackBar("网络好像不太好哦~")
            }
        })
    }

    fun enableNotification() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, applicationInfo.uid)
                putExtra("app_package", packageName)
                putExtra("app_uid", applicationInfo.uid)
            }
        }
        else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        }
        notificationSettingActivity.launch(intent)
    }

    fun connect() {
        val intent = VpnService.prepare(this)
        if (intent == null) {
            v.setIsDisabledConnect(true)
            ConnectorHandler.startV2Ray(this)
        }
        else {
            vpnPermissionActivity.launch(intent)
        }
    }

    fun logout(show: Boolean = true) {
        val intent = Intent()
        intent.action = Env.SERVICE_CHANNEL
        intent.putExtra("action", Env.STOP_SERVICE)
        sendBroadcast(intent)
        v.setUser(null)
        if (show) {
            v.showSnackBar("登录已经失效了（")
        }
    }

    fun install(packageName: String) {
        ClientUtil.open(this, when (packageName) {
            "com.vmos.openapp" -> "https://vpc.sparklefantasia.com/static/VMOS-2.0.0.apk"
            "com.aniplex.kirarafantasia" -> "https://vpc.sparklefantasia.com/static/Kirara-3.6.0.apk"
            else -> ""
        })
    }
}
