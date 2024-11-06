package com.king250.kirafan.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.king250.kirafan.BuildConfig
import com.king250.kirafan.R
import com.king250.kirafan.model.data.AboutItem
import com.king250.kirafan.model.data.DeveloperItem
import com.king250.kirafan.ui.theme.KiraraFansTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KiraraFansTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
fun Main(a: AboutActivity) {
    val scrollState = rememberScrollState()
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
    val items = listOf(
        AboutItem("获得最新版") {
            val intent = CustomTabsIntent.Builder().build()
            val uri = Uri.parse("https://github.com/gd1000m/Kirara-Repo/releases/latest")
            intent.launchUrl(a, uri)
        },
        AboutItem("一起来玩") {
            val intent = CustomTabsIntent.Builder().build()
            val uri = Uri.parse("https://discord.gg/YmbbxDsbNB")
            intent.launchUrl(a, uri)
        },
        AboutItem("项目地址") {
            val intent = CustomTabsIntent.Builder().build()
            val uri = Uri.parse("https://gitlab.com/kirafan/sparkle/server")
            intent.launchUrl(a, uri)
        },
        AboutItem("开源许可证") {
            val intent = Intent(a, LicenseActivity::class.java)
            a.startActivity(intent)
        }
    )
    val developers = listOf(
        DeveloperItem("耀", "https://avatars.githubusercontent.com/u/61256966", "https://github.com/250king"),
        DeveloperItem("Dosugamea", "https://avatars.githubusercontent.com/u/17107514", "https://github.com/Dosugamea"),
        DeveloperItem("misaka10843", "https://avatars.githubusercontent.com/u/69132853", "https://github.com/misaka10843"),
        DeveloperItem("y52en", "https://avatars.githubusercontent.com/u/61645319", "https://github.com/y52en"),
        DeveloperItem("He Li", "https://avatars.githubusercontent.com/u/53819558", "https://github.com/lihe07")
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
                            .data(data = R.drawable.ic_launcher_round)
                            .apply(
                                block = fun ImageRequest.Builder.() {
                                    crossfade(true)
                                }
                            )
                            .build(),
                        imageLoader = imageLoader
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
            developers.forEach { item ->
                ListItem(
                    modifier = Modifier.clickable {
                        val intent = CustomTabsIntent.Builder().build()
                        intent.launchUrl(a, Uri.parse(item.url))
                    },
                    headlineContent = {
                        Text(item.name)
                    },
                    leadingContent = {
                        AsyncImage(
                            modifier = Modifier.clip(CircleShape).size(48.dp),
                            model = item.avatar,
                            contentDescription = null
                        )
                    }
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            items.forEach { item ->
                ListItem(
                    modifier = Modifier.clickable {
                        item.action()
                    },
                    headlineContent = {
                        Text(item.name)
                    }
                )
            }
        }
    }
}
