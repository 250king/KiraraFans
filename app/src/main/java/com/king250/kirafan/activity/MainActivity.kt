package com.king250.kirafan.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.king250.kirafan.R
import com.king250.kirafan.model.UserItem
import com.king250.kirafan.ui.component.CardButton
import com.king250.kirafan.ui.theme.KiraraFansTheme
import com.king250.kirafan.util.ComposableLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KiraraFansTheme {
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
    val profile by remember { mutableStateOf(UserItem()) }
    var version by remember { mutableStateOf("") }
    var unsupportedDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val app = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        "com.vmos.pro"
    }
    else {
        "com.aniplex.kirarafantasia"
    }
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                try {
                    val info = activity.packageManager.getPackageInfo(app, 0)
                    version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        "VMOS ${info.versionName}"
                    }
                    else {
                        info.versionName
                    }
                }
                catch (_: Exception) {
                    version = ""
                }
            }
            else -> {}
        }
    }

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
                        painter = painterResource(R.drawable.smartphone_48px),
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
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                }, onClick = {
                    val intent = Intent(activity, LoginActivity::class.java)
                    activity.startActivity(intent)
                }
            ) {
                Text(profile.name.ifEmpty {"未登录"}, style = MaterialTheme.typography.titleMedium)
                Text("点按${if (profile.name.isEmpty()) { "进行登录" } else { "查看账户详情" }}", style = MaterialTheme.typography.bodyMedium)
            }
            CardButton(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Menu,
                        contentDescription = null
                    )
                },
                onClick = {
                    if (version.isEmpty()) {
                        if (profile.name.isEmpty()) {
                            Toast.makeText(activity, "要登录先才能用哟~", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                unsupportedDialog = true
                            }
                            else {
                                val intent = CustomTabsIntent.Builder().build()
                                val uri = Uri.parse("https://s1.250king.top/achieve/%E3%81%8D%E3%82%89%E3%82%89%E3%83%95%E3%82%A1%E3%83%B3%E3%82%BF%E3%82%B8%E3%82%A2_3.6.0.apk")
                                intent.launchUrl(activity, uri)
                            }
                        }
                    }
                    else {
                        val intent = Intent(Intent.ACTION_DELETE)
                        intent.data = Uri.parse("package:$app")
                        activity.startActivity(intent)
                    }
                }
            ) {
                Text(version.ifEmpty { "应用未安装" }, style = MaterialTheme.typography.titleMedium)
                Text("点按可${if(version.isEmpty()) {"安装"} else {"卸载"}}相关应用", style = MaterialTheme.typography.bodyMedium)
            }
            CardButton(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                },
                onClick = {
                    try {
                        activity.packageManager.getPackageInfo(app, 0)
                        if (profile.name.isEmpty()) {
                            Toast.makeText(activity, "要登录先才能用哟~", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            val intent = activity.packageManager.getLaunchIntentForPackage(app)
                            activity.startActivity(intent)
                        }
                    }
                    catch (_: Exception) {
                        Toast.makeText(activity, "要先安装应用先哟~", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("启动游戏", style = MaterialTheme.typography.titleMedium)
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
                        val intent = CustomTabsIntent.Builder().build()
                        val uri = Uri.parse("https://s1.250king.top/achieve/VMOSPro_3.0.7.apk")
                        intent.launchUrl(activity, uri)
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
