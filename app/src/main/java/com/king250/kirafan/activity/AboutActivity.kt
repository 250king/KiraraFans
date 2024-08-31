package com.king250.kirafan.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import coil.compose.AsyncImage
import com.king250.kirafan.BuildConfig
import com.king250.kirafan.R
import com.king250.kirafan.model.AboutItem
import com.king250.kirafan.model.DeveloperItem
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
fun Main(activity: AboutActivity) {
    val scrollState = rememberScrollState()
    val items = listOf(
        AboutItem("检查更新") {

        },
        AboutItem("给个好评") {

        },
        AboutItem("一起来玩") {
            val intent = CustomTabsIntent.Builder().build()
            val uri = Uri.parse("https://discord.gg/YmbbxDsbNB")
            intent.launchUrl(activity, uri)
        },
        AboutItem("项目地址") {
            val intent = CustomTabsIntent.Builder().build()
            val uri = Uri.parse("https://gitlab.com/kirafan/sparkle/server")
            intent.launchUrl(activity, uri)
        },
        AboutItem("开源许可证") {
            val intent = Intent(activity, LicenseActivity::class.java)
            activity.startActivity(intent)
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
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            activity.finish()
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
                ResourcesCompat.getDrawable(
                    activity.resources,
                    R.mipmap.ic_launcher_round, activity.theme
                )?.let { drawable ->
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth, drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        modifier = Modifier.requiredSize(96.dp).clip(CircleShape),
                        contentDescription = null
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(activity.getString(R.string.app_name), style = MaterialTheme.typography.titleLarge)
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
                    append("都怀念自己的老婆（喜欢Lamp，还有点兔全家桶但都没集全😭）。\n虽然官方留个骨灰盒，但局限性还是有点大，就试想一步步自己搭建私服来复活琪拉拉昔日的光辉。")
                    addStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        start = 0,
                        end = 32
                    )
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
                        intent.launchUrl(activity, Uri.parse(item.url))
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
