package com.king250.kirafan.ui.page

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.king250.kirafan.BuildConfig
import com.king250.kirafan.R
import com.king250.kirafan.model.data.About
import com.king250.kirafan.model.data.Developer
import com.king250.kirafan.activity.AboutActivity
import com.king250.kirafan.activity.LicenseActivity
import com.king250.kirafan.util.ClientUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(a: AboutActivity) {
    val scrollState = rememberScrollState()
    val items = listOf(
        About("一起来玩") {
            ClientUtil.open(a, "https://discord.gg/YmbbxDsbNB")
        },
        About("项目网站") {
            ClientUtil.open(a, "https://sparklefantasia.com/")
        },
        About("服务器源代码") {
            ClientUtil.open(a, "https://gitlab.com/kirafan/sparkle/server")
        },
        About("使用须知") {
            // TODO
        },
        About("开源许可证") {
            val intent = Intent(a, LicenseActivity::class.java)
            a.startActivity(intent)
        }
    )
    val developers = listOf(
        Developer("耀", "https://avatars.githubusercontent.com/u/61256966", "https://github.com/250king"),
        Developer("Dosugamea", "https://avatars.githubusercontent.com/u/17107514", "https://github.com/Dosugamea"),
        Developer("misaka10843", "https://avatars.githubusercontent.com/u/69132853", "https://github.com/misaka10843"),
        Developer("y52en", "https://avatars.githubusercontent.com/u/61645319", "https://github.com/y52en"),
        Developer("He Li", "https://avatars.githubusercontent.com/u/53819558", "https://github.com/lihe07")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest
                            .Builder(a)
                            .data(R.drawable.ic_launcher_round)
                            .apply {
                                crossfade(true)
                            }
                            .build()
                    ),
                    modifier = Modifier.requiredSize(96.dp).clip(CircleShape),
                    contentDescription = null
                )
                Spacer(Modifier.height(16.dp))
                Text(a.getString(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                modifier = Modifier.padding(16.dp),
                text = "项目目的",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 24.sp,
                text = buildAnnotatedString {
                    append("相信过来用这个App的都怀念自己的老婆（点兔全家桶但都没集全😭）。很可惜，官方于2023年2月28日15点59分关服。\n")
                    append("虽然官方留个骨灰盒，但局限性还是有点大，就试想一步步自己搭建私服来复活琪拉拉昔日的光辉。")
                }
            )
            Text(
                modifier = Modifier.padding(16.dp),
                text = "开发者",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            developers.forEach {
                ListItem(
                    modifier = Modifier.clickable {
                        ClientUtil.open(a, it.url)
                    },
                    headlineContent = {
                        Text(it.name)
                    },
                    leadingContent = {
                        AsyncImage(
                            modifier = Modifier.clip(CircleShape).size(48.dp),
                            model = ImageRequest
                                .Builder(a)
                                .data(it.avatar)
                                .apply {
                                    crossfade(true)
                                }
                                .build(),
                            contentDescription = null
                        )
                    }
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            items.forEach {
                ListItem(
                    modifier = Modifier.clickable {
                        it.action()
                    },
                    headlineContent = {
                        Text(it.name)
                    }
                )
            }
        }
    }
}
