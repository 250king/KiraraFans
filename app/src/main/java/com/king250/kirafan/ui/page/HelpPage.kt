package com.king250.kirafan.ui.page

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.king250.kirafan.R
import com.king250.kirafan.ui.activity.HelpActivity
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpPage(a: HelpActivity) {
    val scrollState = rememberScrollState()
    val refresh by a.h.refresh.collectAsState()
    val loading by a.h.loading.collectAsState()
    val content by a.h.content.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Q&A")
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
                            a.h.setRefresh(true)
                            a.h.fetch()
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
