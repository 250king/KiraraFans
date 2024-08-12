package com.king250.kirafan

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.king250.kirafan.activity.AboutActivity
import com.king250.kirafan.activity.InfoActivity
import com.king250.kirafan.ui.theme.KiraraToolsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KiraraToolsTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Main(this)
                }
            }
        }
    }
}

@Composable
@Suppress("FunctionName")
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
fun Main(activity: MainActivity) {
    var version by remember { mutableStateOf("") }
    var unsupportedDialog by remember { mutableStateOf(false) }
    var requiredDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {}
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> {
                try {
                    val info = activity.packageManager.getPackageInfo("com.aniplex.kirarafantasia", 0)
                    version = info.versionName
                }
                catch (_: Exception) {
                    version = ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("KiraraTools")
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).verticalScroll(scrollState)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = {
                    val intent = Intent(activity, InfoActivity::class.java)
                    activity.startActivity(intent)
                }
            ) {
                Row(Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .background(MaterialTheme.colorScheme.inversePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.smartphone_48px),
                            contentDescription = null
                        )
                    }
                    Box(Modifier.width(16.dp))
                    Column(Modifier.align(Alignment.CenterVertically)) {
                        Text("Android ${Build.VERSION.RELEASE}", style = MaterialTheme.typography.titleMedium)
                        Text("点按可查看设备详情", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = {
                    if (version.isEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            unsupportedDialog = true
                        }
                        else {
                            val intent = CustomTabsIntent.Builder().build()
                            val uri = Uri.parse("https://s1.250king.top/achieve/%E3%81%8D%E3%82%89%E3%82%89%E3%83%95%E3%82%A1%E3%83%B3%E3%82%BF%E3%82%B8%E3%82%A2_3.6.0.apk")
                            intent.launchUrl(activity, uri)
                        }
                    }
                    else {
                        val uri = Uri.fromParts("package", "com.aniplex.kirarafantasia", null)
                        val intent = Intent(Intent.ACTION_DELETE, uri)
                        activity.startActivity(intent)
                    }
                }
            ) {
                Row(Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .background(MaterialTheme.colorScheme.inversePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.stadia_controller_24px),
                            contentDescription = null
                        )
                    }
                    Box(Modifier.width(16.dp))
                    Column(Modifier.align(Alignment.CenterVertically)) {
                        Text(
                            text = version.ifEmpty { "应用未安装" },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("点按可安装/卸载游戏本体", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = { }
            ) {
                Row(Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .background(MaterialTheme.colorScheme.inversePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                    }
                    Box(Modifier.width(16.dp))
                    Column(Modifier.align(Alignment.CenterVertically)) {
                        Text("启动游戏", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = { }
            ) {
                Row(Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .background(MaterialTheme.colorScheme.inversePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null
                        )
                    }
                    Box(Modifier.width(16.dp))
                    Column(Modifier.align(Alignment.CenterVertically)) {
                        Text("骨灰盒导入/导出", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = {
                    val intent = Intent(activity, AboutActivity::class.java)
                    activity.startActivity(intent)
                }
            ) {
                Row(Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .background(MaterialTheme.colorScheme.inversePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Filled.Info,
                            contentDescription = null
                        )
                    }
                    Box(Modifier.width(16.dp))
                    Column(Modifier.align(Alignment.CenterVertically)) {
                        Text("关于KiraraFans", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    if (unsupportedDialog) {
        AlertDialog(
            onDismissRequest = { },
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
}
