package com.king250.kirafan.ui.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonParser
import com.king250.kirafan.Env
import com.king250.kirafan.dataStore
import com.king250.kirafan.model.data.UserItem
import com.king250.kirafan.model.view.LoginState
import com.king250.kirafan.ui.theme.KiraraFansTheme
import com.king250.kirafan.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

class LoginActivity : ComponentActivity() {
    val loginState: LoginState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            loginState.token.collect { token ->
                try {
                    withContext(Dispatchers.IO) {
                        val request = Request
                            .Builder()
                            .url("${Env.QLOGIN_API}/me")
                            .header("Authorization", "Bearer $token")
                            .build()
                        val response = Utils.httpClient.newCall(request).execute()
                        val result = JsonParser.parseString(response.body?.string()!!).asJsonObject
                        val intent = Intent()
                        val user = UserItem(
                            result.get("name").asString,
                            result.get("avatar").asString
                        )
                        response.close()
                        intent.putExtra("profile", user)
                        setResult(RESULT_OK, intent)
                        dataStore.edit {
                            it[stringPreferencesKey("token")] = token
                        }
                        finish()
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "网络好像不太好，退出再试试吧~", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        setContent {
            KiraraFansTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Main(this)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        loginState.start()
    }

    override fun onStop() {
        super.onStop()
        loginState.stop()
    }
}

@Suppress("FunctionName")
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(activity: LoginActivity) {
    val code by activity.loginState.code.collectAsState()
    val time by activity.loginState.time.collectAsState()
    val anime by activity.loginState.anime.collectAsState()
    val activate by activity.loginState.activate.collectAsState()
    val now by animateFloatAsState(
        targetValue = time,
        label = "",
        animationSpec = tween(durationMillis = anime, easing = LinearEasing)
    )

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
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            if (activate) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = {now})
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(code, style = MaterialTheme.typography.displayLarge)
                Text("请向各个可用平台发送该代码完成登录", Modifier.padding(vertical = 16.dp))
                TextButton(
                    onClick = {
                        if (code.matches(Regex("^[0-9]+$"))) {
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
}
