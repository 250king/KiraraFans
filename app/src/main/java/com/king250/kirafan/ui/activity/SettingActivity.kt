package com.king250.kirafan.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.king250.kirafan.R
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.model.view.SettingState
import com.king250.kirafan.ui.theme.KiraraFansTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingActivity : ComponentActivity() {
    val s: SettingState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            s.init()
        }
        setContent {
            KiraraFansTheme {
                Main(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(a: SettingActivity) {
    val scrollState = rememberScrollState()
    val snackBarHostState = remember { SnackbarHostState() }
    val dns by a.s.dns.collectAsState()

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text("设置")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            a.finish()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).verticalScroll(scrollState)) {
            ListItem(
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.dns),
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text("DNS over HTTPS")
                },
                supportingContent = {
                    Text("有可能会增加延迟，所以仅适用于所在地区默认DNS记录受到污染的情况下使用")
                },
                trailingContent = {
                    Switch(
                        checked = dns,
                        onCheckedChange = { value ->
                            CoroutineScope(Dispatchers.IO).launch {
                                a.s.setDns(value)
                            }
                        }
                    )
                }
            )
            ListItem(
                modifier = Modifier.clickable {
                    ClientUtil.open(a, "https://github.com/gd1000m/Kirara-Repo/releases/latest")
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.update),
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text("获得最新版本")
                }
            )
            ListItem(
                modifier = Modifier.clickable {
                    val intent = Intent(a, AboutActivity::class.java)
                    a.startActivity(intent)
                },
                leadingContent = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Info,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text("关于")
                }
            )
        }
    }
}
