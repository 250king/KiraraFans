package com.king250.kirafan.ui.page

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.king250.kirafan.R
import com.king250.kirafan.ui.activity.LicenseActivity
import com.king250.kirafan.ui.activity.LicenseDetailActivity
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensePage(a: LicenseActivity) {
    val origin = Libs.Builder().withContext(a).build()
    val filter = origin.libraries.filter { library ->
        library.licenses.isNotEmpty()
    }

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
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LibrariesContainer(
            libraries = Libs(filter.toImmutableList(), origin.licenses),
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
