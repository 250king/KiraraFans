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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.king250.kirafan.Env
import com.king250.kirafan.R
import com.king250.kirafan.ui.activity.AboutActivity
import com.king250.kirafan.ui.activity.InfoActivity
import com.king250.kirafan.ui.activity.MainActivity
import com.king250.kirafan.dataStore
import com.king250.kirafan.ui.component.CardButton
import com.king250.kirafan.ui.dialog.*
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.api.HttpApi
import com.king250.kirafan.ui.activity.HelpActivity
import com.king250.kirafan.util.IpcUtil
import com.king250.kirafan.util.StringUtil
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    var disabledLogin by remember { mutableStateOf(false) }
    val disabledConnect by a.m.disabledConnect.collectAsState()
    val selectedEndpoint by a.m.selectedEndpoint.collectAsState()
    val endpoints by a.m.endpoints.collectAsState()
    val connected by a.m.connected.collectAsState()
    val version by a.m.version.collectAsState()
    val loading by a.m.loading.collectAsState()
    val update by a.m.update.collectAsState()
    val user by a.m.user.collectAsState()

    fun login() {
        if (!disabledLogin) {
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
                disabledLogin = true
                scope.launch {
                    val token = a.dataStore.data
                        .map{it[stringPreferencesKey("refresh_token")]}
                        .firstOrNull() ?: ""
                    HttpApi.oauth.logout(token).enqueue(object : Callback<Unit> {
                        override fun onResponse(p0: Call<Unit>, p1: Response<Unit>) {
                            scope.launch {
                                a.dataStore.edit {
                                    it.remove(booleanPreferencesKey("agreed"))
                                }
                                a.logout(false)
                                disabledLogin = false
                            }
                        }

                        override fun onFailure(p0: Call<Unit>, p1: Throwable) {
                            scope.launch {
                                snackBarHostState.showSnackbar("网络好像不太好哦~")
                                disabledLogin = false
                            }
                        }
                    })
                }
            }
        }
    }
    fun install() {
        if (version == null) {
            if (Env.HEIGHT_ANDROID) {
                a.d.openSystem(true)
            }
            else {
                a.install(Env.TARGET_PACKAGE)
            }
        }
        else {
            if (Env.TARGET_PACKAGE == "com.aniplex.kirarafantasia") {
                if (version != "3.6.0") {
                    a.d.openGame(true)
                }
                else if (ClientUtil.isRooted()) {
                    a.d.openRoot(true)
                }
                else if (ClientUtil.isDebug(a.contentResolver)) {
                    a.d.openUsb(true)
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
        if (!disabledConnect) {
            if (connected) {
                IpcUtil.toService(a, Env.STOP_SERVICE)
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
    fun about() {
        val intent = Intent(a, AboutActivity::class.java)
        a.startActivity(intent)
    }
    fun info() {
        val intent = Intent(a, InfoActivity::class.java)
        a.startActivity(intent)
    }

    a.m.setSnackBarHostState(snackBarHostState)

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
        Crossfade(targetState = loading, label = "") { loading ->
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
                val text = if (Env.HEIGHT_ANDROID) {
                    "Android 14+建议阅读"
                }
                else {
                    "有任何问题可查看"
                }
                Column(Modifier.padding(innerPadding).verticalScroll(scrollState)) {
                    AnimatedVisibility(update) {
                        CardButton(
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.update),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                ClientUtil.open(a, "https://250king.top/s/bdZ4pT9jV0")
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
                                if (disabledConnect) {
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
                            description = if(connected) {"已连接"} else {"未连接"}
                        )
                    }
                    AnimatedVisibility(connected) {
                        CardButton(
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.lan),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                a.d.openSelector(true)
                            },
                            title = "节点",
                            description = endpoints[selectedEndpoint].name
                        )
                    }
                    AnimatedVisibility(connected) {
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
                                painter = painterResource(R.drawable.developer_guide),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            val intent = Intent(a, HelpActivity::class.java)
                            a.startActivity(intent)
                        },
                        title = "Q&A",
                        description = text
                    )
                    CardButton(
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Default.Info,
                                contentDescription = null
                            )
                        },
                        onClick = {about()},
                        title = "关于"
                    )
                }
            }
        }
    }
    VersionBadDialog(a)
    SystemDialog(a)
    SelectDialog(a)
    RootDialog(a)
    UsbDialog(a)
}
