package com.king250.kirafan.ui.page

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.king250.kirafan.model.data.Info
import com.king250.kirafan.ui.activity.InfoActivity
import com.king250.kirafan.util.ClientUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPage(a: InfoActivity) {
    val scrollState = rememberScrollState()
    val items = listOf(
        Info("Android版本", Build.VERSION.RELEASE),
        Info("Android API级别", Build.VERSION.SDK_INT.toString()),
        Info(
            name = "USB调试",
            value = if (ClientUtil.isDebug(a.contentResolver)) {
                "已打开"
            }
            else {
                "未打开"
            }
        ),
        Info(
            name = "Root",
            value = if (ClientUtil.isRooted()) {
                "可用"
            }
            else {
                "不可用"
            }
        ),
        Info("厂商", Build.MANUFACTURER),
        Info("品牌", Build.BRAND),
        Info("型号", Build.MODEL),
        Info("基板", Build.BOARD),
        Info("设备指纹", Build.FINGERPRINT),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("设备信息")
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
            for (item in items) {
                ListItem(
                    modifier = Modifier.clickable {
                        val cm = a.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText(null, item.value))
                        ClientUtil.toast(a, "已成功复制到剪切板")
                    },
                    headlineContent = {
                        Text(item.name)
                    },
                    supportingContent = {
                        Text(item.value)
                    }
                )
            }
        }
    }
}
