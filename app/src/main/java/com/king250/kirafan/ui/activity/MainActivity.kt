package com.king250.kirafan.ui.activity

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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonParser
import com.king250.kirafan.Env
import com.king250.kirafan.Util
import com.king250.kirafan.dataStore
import com.king250.kirafan.model.view.MainState
import com.king250.kirafan.service.ConnectorServiceManager
import com.king250.kirafan.ui.page.AbiWarning
import com.king250.kirafan.ui.page.Home
import com.king250.kirafan.ui.theme.KiraraFansTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var compatSplashScreen: SplashScreen

    private lateinit var vpnPermissionActivity: ActivityResultLauncher<Intent>

    private lateinit var notificationSettingActivity: ActivityResultLauncher<Intent>

    lateinit var permissionActivity: ActivityResultLauncher<String>

    lateinit var apkAbi: String

    lateinit var content: Unit

    val deviceAbi: String = Build.SUPPORTED_ABIS[0]

    val isSpecial = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val app = if (isSpecial) {"com.vmos.pro"} else {"com.aniplex.kirarafantasia"}

    val v: MainState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        compatSplashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        apkAbi = when (File(applicationInfo.nativeLibraryDir).name) {
            "arm64" -> "arm64-v8a"
            "arm" -> "armeabi-v7a"
            else -> File(applicationInfo.nativeLibraryDir).name
        }
        if (deviceAbi == apkAbi) {
            vpnPermissionActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    ConnectorServiceManager.startV2Ray(this)
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
                        Util.toast(this, "由于你已经设置不允许再请求权限了，所以只能你自己设置了（")
                        enableNotification()
                    }
                }
            }
            onBackPressedDispatcher.addCallback(this) {
                moveTaskToBack(false)
            }
            setContent {
                KiraraFansTheme {
                    Home(this)
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
            val info = packageManager.getPackageInfo(app, 0)
            v.setVersion(if (isSpecial) {"VMOS ${info.versionName}"} else {info.versionName})
        }
        catch (_: Exception) {
            v.setVersion(null)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.data != null && intent.action == Intent.ACTION_VIEW) {
            try {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        v.setIsLoading(true)
                        val code = intent.data!!.getQueryParameter("code") ?: ""
                        val body = FormBody
                            .Builder()
                            .add("grant_type", "authorization_code")
                            .add("redirect_uri", Env.REDIRECT_URI)
                            .add("code", code)
                            .build()
                        val request = Request
                            .Builder()
                            .url("${Env.SERVER_API}/oauth2/token")
                            .header("Authorization", "Basic ${Env.BASIC_AUTH}")
                            .post(body)
                            .build()
                        val response = Util.http(this@MainActivity).newCall(request).execute()
                        if (response.code == 200) {
                            val data = JsonParser.parseString(response.body?.string() ?: "").asJsonObject
                            dataStore.edit {
                                it[stringPreferencesKey("access_token")] = data.get("access_token").asString
                                it[stringPreferencesKey("refresh_token")] = data.get("refresh_token").asString
                            }
                            v.refresh()
                        }
                        response.close()
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                v.showSnackBar("网络好像不太好哦~")
            }
        }
        v.setIsLoading(false)
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
            ConnectorServiceManager.startV2Ray(this)
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
        v.setIsDisabledInstall(true)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val request = Request.Builder().url("${Env.SERVER_API}/1.0/file/aliyun/archive/$packageName.apk").build()
                    val response = Util.http(this@MainActivity, true).newCall(request).execute()
                    if (response.code == 200) {
                        val result = JsonParser.parseString(response.body?.string()).asJsonObject
                        Util.open(this@MainActivity, result.get("url").asString)
                    }
                    else {
                        logout()
                    }
                    response.close()
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                v.showSnackBar("网络好像不太好哦~")
            }
            v.setIsDisabledInstall(false)
        }
    }
}
