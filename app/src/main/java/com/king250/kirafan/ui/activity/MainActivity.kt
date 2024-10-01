package com.king250.kirafan.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.google.gson.JsonParser
import com.king250.kirafan.BuildConfig
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
import java.io.File

class MainActivity : ComponentActivity() {
    lateinit var loginActivity: ActivityResultLauncher<Intent>

    lateinit var permissionActivity: ActivityResultLauncher<Intent>

    private lateinit var compatSplashScreen: SplashScreen

    lateinit var apkAbi: String

    val deviceAbi: String = Build.SUPPORTED_ABIS[0]

    val isSpecial = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val app = if (isSpecial) {"com.vmos.pro"} else {"com.aniplex.kirarafantasia"}

    val mainState: MainState by viewModels()

    @OptIn(ExperimentalCoilApi::class)
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        compatSplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        apkAbi = when (File(applicationInfo.nativeLibraryDir).name) {
            "arm64" -> "arm64-v8a"
            "arm" -> "armeabi-v7a"
            else -> File(applicationInfo.nativeLibraryDir).name
        }
        if (deviceAbi == apkAbi) {
            compatSplashScreen.setKeepOnScreenCondition {
                mainState.isDisableLogin.value
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
                        Main(this@MainActivity)
                    }
                }
            }
            lifecycleScope.launch {
                val token = dataStore.data.map{it[stringPreferencesKey("token")]}.firstOrNull()
                mainState.init()
                if (token == null) {
                    mainState.disableLogin(false)
                }
                else {
                    mainState.fetch(token)
                }
                mainState.check()
                mainState.upgrade.collect { version ->
                    if (version != BuildConfig.VERSION_NAME) {
                        Toast.makeText(this@MainActivity, "发现新版本了！", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else {
            compatSplashScreen.setKeepOnScreenCondition{false}
            setContent {
                KiraraFansTheme {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Unsupported(this)
                    }
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
            try {
                withContext(Dispatchers.IO) {
                    val token = dataStore.data.map {it[stringPreferencesKey("token")]}.firstOrNull()
                    val request = Request
                        .Builder()
                        .url("${Env.KIRARA_API}/file/$packageName")
                        .header("Authorization", "Bearer $token")
                        .build()
                    val response = Utils.httpClient.newCall(request).execute()
                    val result = JsonParser.parseString(response.body?.string()).asJsonObject
                    val url = result.get("url").asString
                    val intent = CustomTabsIntent.Builder().build()
                    val uri = Uri.parse(url)
                    intent.launchUrl(this@MainActivity, uri)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "网络好像不太好，退出再试试吧~", Toast.LENGTH_SHORT).show()
            }
            mainState.disableDownload(false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun Main(activity: MainActivity) {
    val scrollState = rememberScrollState()
    val user by activity.mainState.user.collectAsState()
    val version by activity.mainState.version.collectAsState()
    val isConnect by activity.mainState.isConnect.collectAsState()
    val isLoginState by activity.mainState.isDisableLogin.collectAsState()
    val isDownloadDisable by activity.mainState.isDisableDownload.collectAsState()
    var unsupportedDialog by remember { mutableStateOf(false) }
    var usbWarningDialog by remember { mutableStateOf(false) }
    var incurredDialog by remember { mutableStateOf(false) }

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
            Spacer(Modifier.height(16.dp))
            CardButton(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.phone),
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
                        painter = painterResource(R.drawable.controller),
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
                        if (activity.app == "com.aniplex.kirarafantasia" && version != "3.6.0") {
                            incurredDialog = true
                        }
                        else {
                            if (activity.app == "com.aniplex.kirarafantasia" && Utils.getUSB(activity.contentResolver)) {
                                usbWarningDialog = true
                            }
                            else {
                                val intent = activity.packageManager.getLaunchIntentForPackage(activity.app)
                                activity.startActivity(intent)
                            }
                        }
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
                        painter = painterResource(R.drawable.cable),
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
                unsupportedDialog = false
            },
            title = {
                Text("提示")
            },
            text = {
                Text(
                    text = "因为游戏不允许Android 14+的设备运行。只能通过安装VMOS并在内部安装好游戏后继续游玩。请问是否要继续下载VMOS并安装？",
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
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    if (usbWarningDialog) {
        AlertDialog(
            onDismissRequest = {
                usbWarningDialog = false
            },
            title = {
                Text("提示")
            },
            text = {
                Text(
                    text = "你好像没有把USB调试给关闭，这会导致游戏自动闪退，请问要前往设置关闭还是继续游玩？",
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                        activity.startActivity(intent)
                        usbWarningDialog = false
                    }
                ) {
                    Text("关闭USB调试")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val intent = activity.packageManager.getLaunchIntentForPackage(activity.app)
                        activity.startActivity(intent)
                        usbWarningDialog = false
                    }
                ) {
                    Text("继续游玩")
                }
            }
        )
    }
    if (incurredDialog) {
        AlertDialog(
            onDismissRequest = {
                incurredDialog = false
            },
            title = {
                Text("提示")
            },
            text = {
                if (version == "3.7.0") {
                    val text = """
                        你好像使用的是骨灰盒版本，这个版本是无法正常游戏的，只能删掉并安装正确的版本。
                        由于这是你最珍贵的存档之一，我们非常不推荐以抛弃骨灰盒的代价来玩，建议换别的设备继续玩吧！
                        如果你已知悉相关风险或者确定骨灰盒没有任何内容，请自行将骨灰盒删除后再试。
                    """.trimIndent()
                    Text(text, lineHeight = 24.sp)
                }
                else {
                    Text("只有3.6.0才能正常使用哟！是否要下载并安装对应的版本？", lineHeight = 24.sp)
                }
            },
            confirmButton = {
                if (version != "3.7.0") {
                    TextButton(
                        onClick = {
                            incurredDialog = false
                            activity.download(activity.app)
                        }
                    ) {
                        Text("确定")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        incurredDialog = false
                    }
                ) {
                    if (version == "3.7.0") {
                        Text("关闭")
                    }
                    else {
                        Text("取消")
                    }
                }
            }
        )
    }
}

@Composable
@ExperimentalCoilApi
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun Unsupported(activity: MainActivity) {
    val imageLoader = ImageLoader
        .Builder(activity)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Scaffold { innerPadding ->
        Column(
            Modifier.padding(innerPadding).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(279.dp, 173.dp).clip(RoundedCornerShape(10.dp)),
                painter = rememberAsyncImagePainter(
                    model = ImageRequest
                        .Builder(activity)
                        .data(data = R.drawable.bocchi)
                        .apply(
                            block = fun ImageRequest.Builder.() {
                                crossfade(true)
                            }
                        )
                        .build(),
                    imageLoader = imageLoader
                ),
                contentDescription = null
            )
            Text(
                text = "你好像安装了与设备CPU不匹配的ABI变体，你应该安装${activity.deviceAbi}而不是当前的${activity.apkAbi}",
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}
