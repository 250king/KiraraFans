package com.king250.kirafan.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.king250.kirafan.ui.page.LicenseDetailPage
import com.king250.kirafan.ui.theme.KiraraFansTheme

class LicenseDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiraraFansTheme {
                LicenseDetailPage(this)
            }
        }
    }
}
