package com.king250.kirafan.ui.page

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.king250.kirafan.Env
import com.king250.kirafan.R
import com.king250.kirafan.activity.InfoActivity
import com.king250.kirafan.activity.MainActivity
import com.king250.kirafan.activity.SettingActivity
import com.king250.kirafan.dataStore
import com.king250.kirafan.ui.component.CardButton
import com.king250.kirafan.ui.dialog.*
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.util.HttpUtil
import com.king250.kirafan.util.StringUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(a: MainActivity) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val user by a.v.user.collectAsState()
    val version by a.v.version.collectAsState()
    val endpoints by a.v.endpoints.collectAsState()
    val selectedEndpoint by a.v.selectedEndpoint.collectAsState()
    val isConnect by a.v.isConnected.collectAsState()
    val isLoading by a.v.isLoading.collectAsState()
    val isNeedUpdate by a.v.openUpdate.collectAsState()
    val isDisabledConnect by a.v.isDisabledConnect.collectAsState()
    var isDisabledLogin by remember { mutableStateOf(false) }

    fun login() {
        if (!isDisabledLogin) {
            if (user == null) {
                a.challenge = StringUtil.generateCodeVerifier()
                val redirectUri = URLEncoder.encode(Env.REDIRECT_URI, "utf-8")
                val url = Env.AUTHORIZE_URI +
                        "?response_type=code" +
                        "&client_id=${Env.CLIENT_ID}" +
                        "&redirect_uri=${redirectUri}" +
                        "&code_challenge_method=S256" +
                        "&code_challenge=${StringUtil.generateCodeChallenge(a.challenge!!)}"
                ClientUtil.open(a, url)
            }
            else {
                isDisabledLogin = true
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val token = a.dataStore.data
                            .map{it[stringPreferencesKey("refresh_token")]}
                            .firstOrNull() ?: ""
                        HttpUtil.auth.logout(token).enqueue(object : Callback<Unit> {
                            override fun onResponse(p0: Call<Unit>, p1: Response<Unit>) {
                                a.logout(false)
                                isDisabledLogin = false
                            }

                            override fun onFailure(p0: Call<Unit>, p1: Throwable) {
                                scope.launch {
                                    snackBarHostState.showSnackbar("网络好像不太好哦~")
                                    isDisabledLogin = false
                                }
                            }
                        })
                    }
                }
            }
        }
    }
    fun install() {
        if (version == null) {
            if (Env.HEIGHT_ANDROID) {
                a.v.setIsUnsupported(true)
            }
            else {
                a.install(Env.TARGET_PACKAGE)
            }
        }
        else {
            if (Env.TARGET_PACKAGE == "com.aniplex.kirarafantasia") {
                if (version != "3.6.0") {
                    a.v.setIsVersionBad(true)
                }
                else if (ClientUtil.isRooted()) {
                    a.v.setIsRoot(true)
                }
                else if (ClientUtil.isDebug(a.contentResolver)) {
                    a.v.setIsUsb(true)
                }
                else {
                    val intent = a.packageManager.getLaunchIntentForPackage(Env.TARGET_PACKAGE)
                    a.startActivity(intent)
                }
            }
            else {
                val intent = a.packageManager.getLaunchIntentForPackage(Env.TARGET_PACKAGE)
                a.startActivity(intent)
            }
        }
    }
    fun connect() {
        if (!isDisabledConnect) {
            if (isConnect) {
                val intent = Intent()
                intent.action = Env.SERVICE_CHANNEL
                intent.putExtra("action", Env.STOP_SERVICE)
                a.sendBroadcast(intent)
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(a, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                        a.permissionActivity.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    else {
                        a.connect()
                    }
                }
                else {
                    val enabled = NotificationManagerCompat.from(a).areNotificationsEnabled()
                    if (!enabled) {
                        a.enableNotification()
                    }
                    else {
                        a.connect()
                    }
                }
            }
        }
    }
    fun setting() {
        val intent = Intent(a, SettingActivity::class.java)
        a.startActivity(intent)
    }
    fun info() {
        val intent = Intent(a, InfoActivity::class.java)
        a.startActivity(intent)
    }

    a.v.setSnackBarHostState(snackBarHostState)

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(a.getString(R.string.app_name))
                }
            )
        }
    ) { innerPadding ->
        Crossfade(targetState = isLoading, label = "") { loading ->
            if (loading) {
                Column(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            else {
                Column(Modifier.padding(innerPadding).verticalScroll(scrollState)) {
                    AnimatedVisibility(isNeedUpdate) {
                        CardButton(
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.update),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                ClientUtil.open(a, "https://github.com/gd1000m/Kirara-Repo/releases/latest")
                            },
                            title = "有新版本发布了",
                            description = "点击即可下载最新版"
                        )
                    }
                    CardButton(
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.phone),
                                contentDescription = null
                            )
                        },
                        onClick = {info()},
                        title = "Android ${Build.VERSION.RELEASE}",
                        description = "点击可查看设备详情"
                    )
                    CardButton(
                        icon = {
                            if (user == null) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            }
                            else {
                                AsyncImage(
                                    modifier = Modifier.clip(CircleShape).size(48.dp),
                                    model = ImageRequest
                                        .Builder(a)
                                        .data(user!!.avatar)
                                        .apply {
                                            crossfade(true)
                                        }
                                        .build(),
                                    contentDescription = null
                                )
                            }
                        },
                        onClick = {login()},
                        title = user?.name ?: "未登录",
                        description = "点击${if (user == null) {"进行登录"} else {"退出登录"}}",
                    )
                    AnimatedVisibility(user != null) {
                        CardButton(
                            icon = {
                                if (isDisabledConnect) {
                                    CircularProgressIndicator()
                                }
                                else {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(R.drawable.cable),
                                        contentDescription = null
                                    )
                                }
                            },
                            onClick = {connect()},
                            title = "连接至专网",
                            description = if(isConnect) {"已连接"} else {"未连接"}
                        )
                    }
                    AnimatedVisibility(isConnect) {
                        CardButton(
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.lan),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                a.v.setOpenSelect(true)
                            },
                            title = "节点",
                            description = endpoints[selectedEndpoint].name
                        )
                    }
                    AnimatedVisibility(isConnect) {
                        CardButton(
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.controller),
                                    contentDescription = null
                                )
                            },
                            onClick = {install()},
                            title = version ?: "未安装",
                            description = if(version == null) {"点按可安装"} else {"启动游戏"}
                        )
                    }
                    CardButton(
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Default.Settings,
                                contentDescription = null
                            )
                        },
                        onClick = {setting()},
                        title = "设置"
                    )
                }
            }
        }
    }

    UnsupportedDialog(a)
    VersionBadDialog(a)
    RootDialog(a)
    UsbDialog(a)
    SelectDialog(a)
}
