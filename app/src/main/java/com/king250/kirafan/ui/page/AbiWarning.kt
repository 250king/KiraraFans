package com.king250.kirafan.ui.page

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
import com.king250.kirafan.R
import com.king250.kirafan.Util
import com.king250.kirafan.ui.activity.MainActivity

@Composable
fun AbiWarning(a: MainActivity) {
    val imageLoader = ImageLoader
        .Builder(a)
        .crossfade(true)
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
                text = "你好像安装了与设备CPU不匹配的ABI变体，你应该安装${a.deviceAbi}而不是当前的${a.apkAbi}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            TextButton(
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    Util.open(a, "https://github.com/gd1000m/Kirara-Repo/releases/latest")
                }
            ) {
                Text("前往下载正确的版本")
            }
        }
    }
}
