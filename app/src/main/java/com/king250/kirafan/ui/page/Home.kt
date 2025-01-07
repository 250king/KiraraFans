package com.king250.kirafan.ui.page

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.dataStore
import com.king250.kirafan.ui.activity.InfoActivity
import com.king250.kirafan.ui.activity.MainActivity
import com.king250.kirafan.ui.activity.SettingActivity
import com.king250.kirafan.ui.component.CardButton
import com.king250.kirafan.ui.component.dialog.RootDialog
import com.king250.kirafan.ui.component.dialog.UnsupportedDialog
import com.king250.kirafan.ui.component.dialog.UsbDialog
import com.king250.kirafan.ui.component.dialog.VersionBadDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(a: MainActivity) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val user by a.v.user.collectAsState()
    val version by a.v.version.collectAsState()
    val isConnect by a.v.isConnected.collectAsState()
    val isLoading by a.v.isLoading.collectAsState()
    val isNeedUpdate by a.v.isNeedUpdate.collectAsState()
    val isDisabledConnect by a.v.isDisabledConnect.collectAsState()
    var isDisabledLogin by remember { mutableStateOf(false) }

    fun loginHandler() {
        if (!isDisabledLogin) {
            if (user == null) {
                val redirectUri = URLEncoder.encode(Env.REDIRECT_URI, "utf-8")
                ClientUtil.open(
                    context = a,
                    url = "${Env.AUTHORIZE_URI}?client_id=${Env.CLIENT_ID}&redirect_uri=${redirectUri}"
                )
            }
            else {
                isDisabledLogin = true
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val refreshToken = a
                            .dataStore
                            .data
                            .map{it[stringPreferencesKey("refresh_token")]}
                            .firstOrNull() ?: ""
                        val body = FormBody
                            .Builder()
                            .add("token", refreshToken)
                            .build()
                        val request = Request
                            .Builder()
                            .url("${Env.SERVER_API}/oauth2/revoke")
                            .header("Authorization", "Basic ${Env.BASIC_AUTH}")
                            .post(body)
                            .build()
                        ClientUtil.http(a).newCall(request).execute()
                        a.logout(false)
                        isDisabledLogin = false
                    }
                }
            }
        }
    }
    fun downloadHandler() {
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
    fun connectHandler() {
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
    fun settingHandler() {
        val intent = Intent(a, SettingActivity::class.java)
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
                    AnimatedVisibility(
                        visible = isNeedUpdate
                    ) {
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
                            }
                        ) {
                            Text(
                                text = "有新版本发布了",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "点按可下载安装最新版",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    CardButton(
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.phone),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            val intent = Intent(a, InfoActivity::class.java)
                            a.startActivity(intent)
                        }
                    ) {
                        Text(
                            text = "Android ${Build.VERSION.RELEASE}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "点按可查看设备详情",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
                        onClick = {loginHandler()}
                    ) {
                        Text(
                            text = user?.name ?: "未登录",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "点击${if (user == null) {"进行登录"} else {"退出登录"}}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    AnimatedVisibility(
                        visible = user != null
                    ) {
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
                            onClick = {connectHandler()}
                        ) {
                            Text("连接至专网", style = MaterialTheme.typography.titleMedium)
                            Text(if(isConnect) {"已连接"} else {"未连接"}, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    AnimatedVisibility(
                        visible = isConnect
                    ) {
                        CardButton(
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.controller),
                                    contentDescription = null
                                )
                            },
                            onClick = {downloadHandler()}
                        ) {
                            Text(
                                text = version ?: "未安装",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if(version == null) {"点按可安装"} else {"启动游戏"},
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    CardButton(
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Default.Settings,
                                contentDescription = null
                            )
                        },
                        onClick = {settingHandler()}
                    ) {
                        Text(
                            text = "设置",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    UnsupportedDialog(a)
    VersionBadDialog(a)
    RootDialog(a)
    UsbDialog(a)
}
