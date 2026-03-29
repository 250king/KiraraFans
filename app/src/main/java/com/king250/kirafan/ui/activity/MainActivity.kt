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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.king250.kirafan.Env
import com.king250.kirafan.api.Api
import com.king250.kirafan.dataStore
import com.king250.kirafan.handler.ConnectorHandler
import com.king250.kirafan.model.view.DialogView
import com.king250.kirafan.model.view.MainView
import com.king250.kirafan.ui.page.Warning
import com.king250.kirafan.ui.page.HomePage
import com.king250.kirafan.ui.theme.KiraraFansTheme
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.util.IpcUtil
import com.king250.kirafan.util.SecurityUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var compatSplashScreen: SplashScreen

    private lateinit var termsActivity: ActivityResultLauncher<Intent>

    private lateinit var vpnPermissionActivity: ActivityResultLauncher<Intent>

    private lateinit var notificationSettingActivity: ActivityResultLauncher<Intent>

    lateinit var permissionActivity: ActivityResultLauncher<String>

    lateinit var apkAbi: String

    var challenge: String? = null

    val m: MainView by viewModels()

    val d: DialogView by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        compatSplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        apkAbi = when (File(applicationInfo.nativeLibraryDir).name) {
            "arm64" -> "arm64-v8a"
            "arm" -> "armeabi-v7a"
            else -> File(applicationInfo.nativeLibraryDir).name
        }
        val tee = SecurityUtil.initKeyStore()
        if (Env.DEVICE_ABI == apkAbi && tee) {
            vpnPermissionActivity =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == RESULT_OK) {
                        connect()
                    } else {
                        m.showSnackBar("这个是程序运行要用到的，所以还是求求你授权吧~")
                    }
                }
            notificationSettingActivity =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val result = ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                        if (result == PackageManager.PERMISSION_DENIED) {
                            m.showSnackBar("这个是程序运行要用到的，所以还是求求你授权吧~")
                        } else {
                            connect()
                        }
                    } else {
                        val enabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
                        if (!enabled) {
                            m.showSnackBar("这个是程序运行要用到的，所以还是求求你授权吧~")
                        } else {
                            connect()
                        }
                    }
                }
            permissionActivity =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    if (it) {
                        connect()
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        ) {
                            m.showSnackBar("这个是程序运行要用到的，所以还是求求你授权吧~")
                        } else {
                            ClientUtil.toast(
                                this,
                                "由于你已经设置不允许再请求权限了，所以只能你自己设置了（"
                            )
                            enableNotification()
                        }
                    }
                }
            termsActivity =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == RESULT_OK) {
                        lifecycleScope.launch {
                            dataStore.edit { preferences ->
                                preferences[booleanPreferencesKey("agreed")] = true
                            }
                            connect()
                        }
                    } else {
                        m.showSnackBar("只有认真阅读且同意了才能玩~")
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
                m.init()
                m.setLoading(false)
                m.check()
            }
        } else {
            setContent {
                KiraraFansTheme {
                    Warning(this, !tee)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val info = packageManager.getPackageInfo(Env.TARGET_PACKAGE, 0)
            m.setVersion(
                if (Env.HEIGHT_ANDROID) {
                    "VMOS ${info.versionName}"
                } else {
                    info.versionName
                }
            )
        } catch (_: Exception) {
            m.setVersion(null)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        callback(intent)
    }

    private fun callback(intent: Intent?) {
        when (intent?.action) {
            Env.OIDC_COMPLETE -> {
                m.setLoading(true)
                Api.oidc.handleRedirect(
                    intent = intent,
                    onSuccess = { token ->
                        lifecycleScope.launch {
                            dataStore.edit {
                                token.accessToken?.let { v -> it[stringPreferencesKey("access_token")] = v }
                                token.refreshToken?.let { v -> it[stringPreferencesKey("refresh_token")] = v }
                                token.idToken?.let { v -> it[stringPreferencesKey("id_token")] = v }
                                token.accessTokenExpirationTime?.let { v -> it[longPreferencesKey("expires_at")] = v }
                            }
                            m.setLoading(false)
                        }
                    },
                    onError = { e ->
                        e.printStackTrace()
                        m.setLoading(false)
                        m.showSnackBar("登录失败，请重试~")
                    }
                )
            }
            Env.OIDC_CANCEL -> {
                m.setLoading(false)
            }
        }
    }

    fun enableNotification() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, applicationInfo.uid)
                putExtra("app_package", packageName)
                putExtra("app_uid", applicationInfo.uid)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                setData(Uri.fromParts("package", packageName, null))
            }
        }
        notificationSettingActivity.launch(intent)
    }

    fun connect() {
        val intent = VpnService.prepare(this)
        if (intent == null) {
            lifecycleScope.launch {
                val agreed = dataStore.data.firstOrNull()?.get(booleanPreferencesKey("agreed"))
                if (agreed == true) {
                    m.setDisabledConnect(true)
                    ConnectorHandler.startVService(this@MainActivity)
                } else {
                    termsActivity.launch(
                        Intent(
                            this@MainActivity,
                            TermsActivity::class.java
                        ).apply {
                            putExtra("show", true)
                        })
                }
            }
        } else {
            vpnPermissionActivity.launch(intent)
        }
    }

    fun logout(show: Boolean = true) {
        IpcUtil.toService(this, Env.STOP_SERVICE)
        m.setUser(null)
        if (show) {
            m.showSnackBar("登录已经失效了（")
        }
    }

    fun change(selected: Int) {
        if (selected != m.selectedEndpoint.value) {
            m.setDisabledConnect(true)
            IpcUtil.toService(this, Env.STOP_SERVICE)
            lifecycleScope.launch {
                delay(500)
                connect()
            }
        }
    }

    fun install(packageName: String) {
        ClientUtil.open(this, "https://api.kirafan.site/v2.1/download/$packageName")
    }
}
