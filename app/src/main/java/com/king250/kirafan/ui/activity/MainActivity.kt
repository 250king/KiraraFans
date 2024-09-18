package com.king250.kirafan.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.alibaba.fastjson2.JSON
import com.king250.kirafan.Env
import com.king250.kirafan.R
import com.king250.kirafan.dataStore
import com.king250.kirafan.model.data.UserItem
import com.king250.kirafan.model.view.MainState
import com.king250.kirafan.service.ConnectorServiceManager
import com.king250.kirafan.ui.component.CardButton
import com.king250.kirafan.ui.theme.KiraraFansTheme
import com.king250.kirafan.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    lateinit var loginActivity: ActivityResultLauncher<Intent>

    lateinit var permissionActivity: ActivityResultLauncher<Intent>

    val isSpecial = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val app = if (isSpecial) {"com.vmos.pro"} else {"com.aniplex.kirarafantasia"}

    val mainState: MainState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainState.init()
        CoroutineScope(Dispatchers.IO).launch {
            val token = dataStore.data.map{it[stringPreferencesKey("token")]}.firstOrNull()
            if (token == null) {
                mainState.disableLogin(false)
            }
            else {
                mainState.fetch(token)
            }
        }
        loginActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val profile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra("profile", UserItem::class.java)!!
                }
                else {
                    it.data?.getParcelableExtra("profile")!!
                }
                mainState.login(profile)
                Toast.makeText(this, "欢迎回来！${profile.name}", Toast.LENGTH_SHORT).show()
            }
        }
        permissionActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                ConnectorServiceManager.startV2Ray(this)
            }
        }
        setContent {
            KiraraFansTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Main(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val info = packageManager.getPackageInfo(app, 0)
            mainState.update(if (isSpecial) {"VMOS ${info.versionName}"} else {info.versionName})
        }
        catch (_: Exception) {
            mainState.update(null)
        }
    }

    fun download(packageName: String) {
        mainState.disableDownload(true)
        lifecycleScope.launch {
            dataStore.data.map {
                it[stringPreferencesKey("token")]
            }.collect {
                val request = Request
                    .Builder()
                    .url("${Env.KIRARA_API}/file/$packageName")
                    .header("Authorization", "Bearer $it")
                    .build()
                withContext(Dispatchers.IO) {
                    val response = Utils.httpClient.newCall(request).execute()
                    val result = JSON.parseObject(response.body?.string())
                    val url = result.getString("url")
                    val intent = CustomTabsIntent.Builder().build()
                    val uri = Uri.parse(url)
                    intent.launchUrl(this@MainActivity, uri)
                    mainState.disableDownload(false)
                }
            }
        }
    }
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
fun Main(activity: MainActivity) {
    val scrollState = rememberScrollState()
    val user by activity.mainState.user.collectAsState()
    val version by activity.mainState.version.collectAsState()
    val isConnect by activity.mainState.isConnect.collectAsState()
    val isLoginState by activity.mainState.isDisableLogin.collectAsState()
    val isDownloadDisable by activity.mainState.isDisableDownload.collectAsState()
    var unsupportedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(activity.getString(R.string.app_name))
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).verticalScroll(scrollState)) {
            CardButton(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.phone_android_24px),
                        contentDescription = null
                    )
                },
                onClick = {
                    val intent = Intent(activity, InfoActivity::class.java)
                    activity.startActivity(intent)
                }
            ) {
                Text("Android ${Build.VERSION.RELEASE}", style = MaterialTheme.typography.titleMedium)
                Text("点按可查看设备详情", style = MaterialTheme.typography.bodyMedium)
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
                            model = user!!.avatar,
                            contentDescription = null
                        )
                    }
                },
                onClick = {
                    if (!isLoginState) {
                        if (user == null) {
                            val intent = Intent(activity, LoginActivity::class.java)
                            activity.loginActivity.launch(intent)
                        }
                        else {
                            activity.mainState.disableLogin(true)
                            val intent = Intent()
                            intent.action = Env.SERVICE_CHANNEL
                            intent.putExtra("action", Env.STOP_SERVICE)
                            activity.sendBroadcast(intent)
                            CoroutineScope(Dispatchers.IO).launch {
                                activity.dataStore.edit { preferences ->
                                    preferences.remove(stringPreferencesKey("token"))
                                }
                                activity.mainState.login(null)
                                activity.mainState.disableLogin(false)
                            }
                        }
                    }
                }
            ) {
                Text(user?.name ?: "未登录", style = MaterialTheme.typography.titleMedium)
                Text("点击${if (user == null) {"进行登录"} else {"退出登录"}}", style = MaterialTheme.typography.bodyMedium)
            }
            CardButton(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.stadia_controller_24px),
                        contentDescription = null
                    )
                },
                onClick = {
                    if (version == null) {
                        if (user == null) {
                            Toast.makeText(activity, "要登录先才能用哟~", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            if (!isDownloadDisable) {
                                if (activity.isSpecial) {
                                    unsupportedDialog = true
                                }
                                else {
                                    activity.download(activity.app)
                                }
                            }
                        }
                    }
                    else {
                        val intent = activity.packageManager.getLaunchIntentForPackage(activity.app)
                        activity.startActivity(intent)
                    }
                }
            ) {
                Text(version ?: "未安装", style = MaterialTheme.typography.titleMedium)
                Text(if(version == null) {"点按可安装"} else {"启动游戏"}, style = MaterialTheme.typography.bodyMedium)
            }
            CardButton(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.cable_24px),
                        contentDescription = null
                    )
                },
                onClick = {
                    if (user == null) {
                        Toast.makeText(activity, "要登录先才能用哟~", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        if (isConnect) {
                            val intent = Intent()
                            intent.action = Env.SERVICE_CHANNEL
                            intent.putExtra("action", Env.STOP_SERVICE)
                            activity.sendBroadcast(intent)
                        }
                        else {
                            val intent = VpnService.prepare(activity)
                            if (intent == null) {
                                ConnectorServiceManager.startV2Ray(activity)
                            }
                            else {
                                activity.permissionActivity.launch(intent)
                            }
                        }
                    }
                }
            ) {
                Text("连接至专网", style = MaterialTheme.typography.titleMedium)
                Text(if(isConnect) {"已连接"} else {"未连接"}, style = MaterialTheme.typography.bodyMedium)
            }
            CardButton(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Info,
                        contentDescription = null
                    )
                },
                onClick = {
                    val intent = Intent(activity, AboutActivity::class.java)
                    activity.startActivity(intent)
                }
            ) {
                Text("关于${activity.getString(R.string.app_name)}", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (unsupportedDialog) {
        AlertDialog(
            onDismissRequest = {
                activity.mainState.disableDownload(false)
            },
            title = {
                Text("提示")
            },
            text = {
                Text(
                    text = "因为游戏自身限定，无法在Android 14+的设备运行。当然也可以通过安装VMOS并安装该启动器来继续游玩。请问是否要继续下载VMOS并安装？",
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        unsupportedDialog = false
                        activity.download(activity.app)
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        unsupportedDialog = false
                        activity.mainState.disableDownload(false)
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}
