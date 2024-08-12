package com.king250.kirafan.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.king250.kirafan.ui.theme.KiraraToolsTheme
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

class LicenseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KiraraToolsTheme {
                // A surface container using the 'background' color from the theme
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
fun Main(activity: LicenseActivity) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("开源许可证")
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
        LibrariesContainer(Modifier.fillMaxSize().padding(innerPadding))
    }
}
