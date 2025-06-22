package com.king250.kirafan.ui.page

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.king250.kirafan.ui.activity.LicenseActivity
import com.king250.kirafan.ui.activity.LicenseDetailActivity
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.util.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensePage(a: LicenseActivity) {
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
            libraries = Libs.Builder().withContext(a).build(),
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
