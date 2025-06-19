package com.king250.kirafan.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.king250.kirafan.model.view.HelpView
import com.king250.kirafan.ui.page.HelpPage
import com.king250.kirafan.ui.theme.KiraraFansTheme

class HelpActivity : ComponentActivity() {
    val h: HelpView by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiraraFansTheme {
                HelpPage(this)
            }
        }
        h.fetch()
    }
}
