package com.king250.kirafan.ui.page

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.king250.kirafan.activity.TermsActivity
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsPage(a: TermsActivity) {
    val scrollState = rememberScrollState()
    var agreed by remember { mutableStateOf(false) }
    val loading by a.t.loading.collectAsState()
    val content by a.t.content.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("使用条款")
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
        },
        bottomBar = {
            val show = a.intent.getBooleanExtra("show", false)

            Crossfade(targetState = loading) { loading ->
                if (!loading && content.isNotEmpty() && show) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Checkbox(
                                checked = agreed,
                                onCheckedChange = {
                                    agreed = it
                                }
                            )
                            Text(
                                text = "我已阅读并同意以上条款",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button (
                                enabled = agreed,
                                onClick = {
                                    a.setResult(Activity.RESULT_OK)
                                    a.finish()
                                },
                            ) {
                                Text("继续")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Crossfade(targetState = loading) { loading ->
            if (loading) {
                Column(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            else {
                if (content.isEmpty()) {
                    Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("加载失败，请返回后再试！")
                    }
                }
                else {
                    Column(Modifier.padding(innerPadding).fillMaxSize().verticalScroll(scrollState)) {
                        MarkdownText(
                            modifier = Modifier.padding(16.dp),
                            markdown = content
                        )
                    }
                }
            }
        }
    }
}
