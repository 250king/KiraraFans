package com.king250.kirafan.ui.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.king250.kirafan.model.data.InfoItem
import com.king250.kirafan.ui.theme.KiraraFansTheme
import com.king250.kirafan.util.Utils

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KiraraFansTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main(this)
                }
            }
        }
    }
}

@Suppress("FunctionName")
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(a: InfoActivity) {
    val scrollState = rememberScrollState()
    val items = listOf(
        InfoItem("厂商", Build.MANUFACTURER),
        InfoItem("品牌", Build.BRAND),
        InfoItem("型号", Build.MODEL),
        InfoItem("基板", Build.BOARD),
        InfoItem("指纹", Build.FINGERPRINT),
        InfoItem(
            "USB调试", if (Utils.checkUSB(a.contentResolver)) {
                "已打开"
            }
            else {
                "未打开"
            }
        ),
        InfoItem("Android版本", Build.VERSION.RELEASE),
        InfoItem("Android API级别", Build.VERSION.SDK_INT.toString()),
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
                        Toast.makeText(a, "已成功复制到剪切板", Toast.LENGTH_SHORT).show()
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
