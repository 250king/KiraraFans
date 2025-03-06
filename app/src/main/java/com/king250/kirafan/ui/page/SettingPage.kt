package com.king250.kirafan.ui.page

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.king250.kirafan.R
import com.king250.kirafan.activity.AboutActivity
import com.king250.kirafan.activity.SettingActivity
import com.king250.kirafan.util.ClientUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(a: SettingActivity) {
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
