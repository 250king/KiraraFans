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
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.king250.kirafan.R
import com.king250.kirafan.ui.activity.TermsActivity
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsPage(a: TermsActivity) {
    val scrollState = rememberScrollState()
    var agreed by remember { mutableStateOf(false) }
    val refresh by a.t.refresh.collectAsState()
    val loading by a.t.loading.collectAsState()
    val content by a.t.content.collectAsState()
    val bottom by remember {
        derivedStateOf {
            scrollState.value >= scrollState.maxValue
        }
    }

    LaunchedEffect(bottom) {
        if (bottom && content.isNotEmpty()) {
            agreed = true
        }
    }

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
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = null
                        )
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
                            Text(
                                text = "请认真阅读并滚动到底面后继续",
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
                    PullToRefreshBox(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        isRefreshing = refresh,
                        onRefresh = {
                            a.t.setRefresh(true)
                            a.t.fetch()
                        }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("加载失败，请下拉刷新再试")
                        }
                    }
                }
                else {
                    Column(Modifier.fillMaxSize().padding(innerPadding).verticalScroll(scrollState)) {
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
