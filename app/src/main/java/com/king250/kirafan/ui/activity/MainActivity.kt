package com.king250.kirafan.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import com.king250.kirafan.Env
import com.king250.kirafan.R
import com.king250.kirafan.dataStore
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
import okhttp3.FormBody
import okhttp3.Request
import java.io.File
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    private lateinit var compatSplashScreen: SplashScreen

    private lateinit var vpnPermissionActivity: ActivityResultLauncher<Intent>

    private lateinit var notificationPermissionActivity: ActivityResultLauncher<Intent>

    lateinit var apkAbi: String

    lateinit var content: Unit

    val deviceAbi: String = Build.SUPPORTED_ABIS[0]

    val isSpecial = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val app = if (isSpecial) {"com.vmos.pro"} else {"com.aniplex.kirarafantasia"}

    val s: MainState by viewModels()

    @OptIn(ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        compatSplashScreen = installSplashScreen()
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
                    toast("这个是程序运行要用到的，所以还是求你授权吧~")
                    s.setIsDisabledConnect(false)
                }
            }
            notificationPermissionActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                            toast("这个是程序运行要用到的，所以还是求你授权吧~")
                        }
                        else {
                            enableVpn()
                        }
                    }
                    else {
                        enableVpn()
                    }
                }
                else {
                    val enabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
                    if (!enabled) {
                        toast("这个是程序运行要用到的，所以还是求你授权吧~")
                    }
                    else {
                        enableVpn()
                    }
                }
            }
            setContent {
                KiraraFansTheme {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Main(this)
                    }
                }
            }
            compatSplashScreen.setKeepOnScreenCondition { false }
            lifecycleScope.launch {
                s.init()
                if (intent.data != null && intent.action == Intent.ACTION_VIEW) {
                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                s.setIsLoading(true)
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
                                val response = Utils.http().newCall(request).execute()
                                if (response.code == 200) {
                                    val data = JsonParser.parseString(response.body?.string() ?: "").asJsonObject
                                    dataStore.edit {
                                        it[stringPreferencesKey("access_token")] = data.get("access_token").asString
                                        it[stringPreferencesKey("refresh_token")] = data.get("refresh_token").asString
                                    }
                                    s.fetch(this@MainActivity)
                                }
                                response.close()
                            }
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                            toast("网络好像不太好，稍后再试试吧~")
                            s.setIsLoading(false)
                        }
                    }
                }
                else {
                    val token = dataStore.data.map{it[stringPreferencesKey("access_token")]}.firstOrNull()
                    if (token == null) {
                        s.setIsLoading(false)
                    }
                    else {
                        s.fetch(this@MainActivity)
                    }
                    s.check()
                }
            }
        }
        else {
            setContent {
                KiraraFansTheme {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Unsupported(this)
                    }
                }
            }
            compatSplashScreen.setKeepOnScreenCondition{ false }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val info = packageManager.getPackageInfo(app, 0)
            s.setVersion(if (isSpecial) {"VMOS ${info.versionName}"} else {info.versionName})
        }
        catch (_: Exception) {
            s.setVersion(null)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == Env.NOTIFICATION_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableVpn()
            }
            else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    toast("这个是程序运行要用到的，所以还是求你授权吧~")
                }
                else {
                    toast("由于你已经设置不再提醒，只能你自己去设置了（")
                    enableNotification()
                }
            }
        }
    }

    private fun enableNotification() {
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
        notificationPermissionActivity.launch(intent)
    }

    private fun enableVpn() {
        val intent = VpnService.prepare(this)
        if (intent == null) {
            s.setIsDisabledConnect(true)
            ConnectorServiceManager.startV2Ray(this)
        }
        else {
            vpnPermissionActivity.launch(intent)
        }
    }


    fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun connect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), Env.NOTIFICATION_PERMISSION_CODE)
            }
            else {
                enableVpn()
            }
        }
        else {
            val enabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
            if (!enabled) {
                enableNotification()
            }
            else {
                enableVpn()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun Main(a: MainActivity) {
    val scrollState = rememberScrollState()
    val user by a.s.user.collectAsState()
    val version by a.s.version.collectAsState()
    val isConnect by a.s.isConnected.collectAsState()
    val isLoading by a.s.isLoading.collectAsState()
    val isDisabledConnect by a.s.isDisabledConnect.collectAsState()
    var isDisabledLogin by remember { mutableStateOf(false) }
    var isDisabledDownload by remember { mutableStateOf(false) }
    var unsupportedDialog by remember { mutableStateOf(false) }
    var usbWarningDialog by remember { mutableStateOf(false) }
    var incurredDialog by remember { mutableStateOf(false) }

    fun install(packageName: String) {
        isDisabledDownload = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.IO) {
                    val request = Request.Builder().url("${Env.KIRARA_API}/file/$packageName").build()
                    val response = Utils.http(a).newCall(request).execute()
                    val result = JsonParser.parseString(response.body?.string()).asJsonObject
                    val url = result.get("url").asString
                    val uri = Uri.parse(url)
                    val intent = CustomTabsIntent.Builder().build()
                    intent.launchUrl(a, uri)
                    response.close()
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                a.toast("网络好像不太好，稍后再试试吧~")
            }
            isDisabledDownload = false
        }
    }
    fun loginHandler() {
        if (!isDisabledLogin) {
            if (user == null) {
                val url = "https://auth.250king.top/auth/authorize?client_id=${Env.CLIENT_ID}&redirect_uri=${URLEncoder.encode(Env.REDIRECT_URI, "utf-8")}"
                val uri = Uri.parse(url)
                val intent = CustomTabsIntent.Builder().build()
                intent.launchUrl(a, uri)
            }
            else {
                CoroutineScope(Dispatchers.IO).launch {
                    isDisabledLogin = true
                    val intent = Intent()
                    val refreshToken = a.dataStore.data.map{it[stringPreferencesKey("refresh_token")]}.firstOrNull()
                    val body = FormBody
                        .Builder()
                        .add("token", refreshToken ?: "")
                        .build()
                    val request = Request
                        .Builder()
                        .url("${Env.SERVER_API}/oauth2/revoke")
                        .header("Authorization", "Basic ${Env.BASIC_AUTH}")
                        .post(body)
                        .build()
                    Utils.http().newCall(request).execute()
                    intent.action = Env.SERVICE_CHANNEL
                    intent.putExtra("action", Env.STOP_SERVICE)
                    a.sendBroadcast(intent)
                    a.dataStore.edit { preferences ->
                        preferences.remove(stringPreferencesKey("access_token"))
                        preferences.remove(stringPreferencesKey("refresh_token"))
                    }
                    a.s.setUser(null)
                    isDisabledLogin = false
                }
            }
        }
    }
    fun downloadHandler() {
        if (version == null) {
            if (user == null) {
                a.toast("要登录先才能用哟~")
            }
            else {
                if (!isDisabledDownload) {
                    if (a.isSpecial) {
                        unsupportedDialog = true
                    }
                    else {
                        install(a.app)
                    }
                }
            }
        }
        else {
            if (a.app == "com.aniplex.kirarafantasia" && version != "3.6.0") {
                incurredDialog = true
            }
            else {
                if (a.app == "com.aniplex.kirarafantasia" && Utils.checkUSB(a.contentResolver)) {
                    usbWarningDialog = true
                }
                else {
                    val intent = a.packageManager.getLaunchIntentForPackage(a.app)
                    a.startActivity(intent)
                }
            }
        }
    }
    fun connectHandler() {
        if (user == null) {
            a.toast("要登录先才能用哟~")
        }
        else {
            if (!isDisabledConnect) {
                if (isConnect) {
                    val intent = Intent()
                    intent.action = Env.SERVICE_CHANNEL
                    intent.putExtra("action", Env.STOP_SERVICE)
                    a.sendBroadcast(intent)
                }
                else {
                    a.connect()
                }
            }
        }
    }
    fun aboutHandler() {
        val intent = Intent(a, AboutActivity::class.java)
        a.startActivity(intent)
    }

    Scaffold(
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
                            val intent = Intent(a, InfoActivity::class.java)
                            a.startActivity(intent)
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
                        onClick = {loginHandler()}
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
                        onClick = {downloadHandler()}
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
                        onClick = {connectHandler()}
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
                        onClick = {aboutHandler()}
                    ) {
                        Text("关于${a.getString(R.string.app_name)}", style = MaterialTheme.typography.titleMedium)
                    }
                }
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
                        install(a.app)
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
                        a.startActivity(intent)
                        usbWarningDialog = false
                    }
                ) {
                    Text("关闭USB调试")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val intent = a.packageManager.getLaunchIntentForPackage(a.app)
                        a.startActivity(intent)
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
                            install(a.app)
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
fun Unsupported(a: MainActivity) {
    val imageLoader = ImageLoader
        .Builder(a)
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
                modifier = Modifier.size(192.dp),
                painter = rememberAsyncImagePainter(
                    model = ImageRequest
                        .Builder(a)
                        .data(data = R.drawable.kuromon)
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
                text = "你好像安装了与设备CPU不匹配的ABI变体，你应该安装${a.deviceAbi}而不是当前的${a.apkAbi}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            TextButton(
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    val intent = CustomTabsIntent.Builder().build()
                    val uri = Uri.parse("https://github.com/gd1000m/Kirara-Repo/releases/latest")
                    intent.launchUrl(a, uri)
                }
            ) {
                Text("前往下载正确的版本")
            }
        }
    }
}
