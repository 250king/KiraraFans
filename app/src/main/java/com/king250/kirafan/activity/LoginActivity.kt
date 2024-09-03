package com.king250.kirafan.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import com.king250.kirafan.model.Env
import com.king250.kirafan.model.UserItem
import com.king250.kirafan.ui.theme.KiraraFansTheme
import com.king250.kirafan.util.ComposableLifecycle
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.sse.RealEventSource
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import java.util.concurrent.TimeUnit

class LoginActivity : ComponentActivity() {
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
fun Main(activity: LoginActivity) {
    lateinit var handler: RealEventSource
    var code by remember { mutableStateOf("") }
    val request = Request.Builder().url("${Env.QLOGIN_HOST}/session").build()
    val client = OkHttpClient
        .Builder()
        .readTimeout(1, TimeUnit.DAYS)
        .connectTimeout(1, TimeUnit.DAYS)
        .build()


    ComposableLifecycle { _, event ->
        when(event) {
            Lifecycle.Event.ON_RESUME -> {
                handler = RealEventSource(request, object : EventSourceListener() {
                    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                        super.onEvent(eventSource, id, type, data)
                        Log.e("kirara", data)
                        when (type) {
                            "code" -> {
                                code = data
                            }
                            "login" -> {
                                val intent = Intent()
                                eventSource.cancel()
                                //获得用户信息
                                intent.putExtra("profile", UserItem(token = data))
                                activity.setResult(Activity.RESULT_OK, intent)
                                activity.finish()
                            }
                            else -> {}
                        }
                    }
                })
                handler.connect(client)
            }
            Lifecycle.Event.ON_PAUSE -> {
                handler.cancel()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("登录")
                },
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
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(code, style = MaterialTheme.typography.displayLarge)
            Text("请向各个可用平台发送该代码完成登录")
            TextButton(
                onClick = {
                    if (code.isNotEmpty()) {
                        val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText(null, code))
                        Toast.makeText(activity, "已成功复制到剪切板", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("复制该代码")
            }
        }
    }
}
