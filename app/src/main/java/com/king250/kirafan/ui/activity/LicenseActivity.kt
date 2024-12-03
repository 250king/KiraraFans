package com.king250.kirafan.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.king250.kirafan.ui.theme.KiraraFansTheme
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

class LicenseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KiraraFansTheme {
                Main(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(a: LicenseActivity) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("开源许可证")
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
        }
    ) { innerPadding ->
        LibrariesContainer(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            onLibraryClick = {
                val intent = Intent(a, LicenseDetailActivity::class.java)
                intent.putExtra("name", it.name)
                intent.putExtra("license", it.licenses.iterator().next().licenseContent)
                a.startActivity(intent)
            }
        )
    }
}
