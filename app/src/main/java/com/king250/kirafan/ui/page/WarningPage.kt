package com.king250.kirafan.ui.page

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.king250.kirafan.Env
import com.king250.kirafan.R
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.ui.activity.MainActivity

@Composable
fun Warning(a: MainActivity, tee: Boolean) {
    val imageLoader = ImageLoader
        .Builder(a)
        .crossfade(true)
        .build()

    Scaffold { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(192.dp),
                painter = rememberAsyncImagePainter(
                    model = ImageRequest
                        .Builder(a)
                        .data(R.drawable.kuromon)
                        .apply {
                            crossfade(true)
                        }
                        .build(),
                    imageLoader = imageLoader
                ),
                contentDescription = null
            )
            Text(
                text = if (tee) {
                    "硬件加密模块无法正常使用，建议升级系统到最新版本，如果解锁Bootloader请检查TEE是否正常工作"
                } else {
                    "你好像安装了与设备CPU不匹配的ABI变体，你应该安装${Env.DEVICE_ABI}而不是当前的${a.apkAbi}"
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            TextButton(
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    if (tee) {
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        a.startActivity(intent)
                    } else {
                        ClientUtil.open(a, "https://2lnk.top/mitmL")
                    }
                }
            ) {
                Text(
                    if (tee) {
                        "前往设置检查更新"
                    } else {
                        "前往下载正确的版本"
                    }
                )
            }
        }
    }
}
