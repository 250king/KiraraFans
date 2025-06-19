package com.king250.kirafan.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.king250.kirafan.ui.page.AboutPage
import com.king250.kirafan.ui.theme.KiraraFansTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiraraFansTheme {
                AboutPage(this)
            }
        }
    }
}
