package com.king250.kirafan.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.king250.kirafan.R
import com.king250.kirafan.ui.activity.LicenseDetailActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseDetailPage(a: LicenseDetailActivity) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(a.intent.getStringExtra("name") ?: "")
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
        Column(Modifier.padding(innerPadding).fillMaxSize().verticalScroll(scrollState)) {
            SelectionContainer(Modifier.padding(16.dp)) {
                Text(a.intent.getStringExtra("license") ?: "")
            }
        }
    }
}
