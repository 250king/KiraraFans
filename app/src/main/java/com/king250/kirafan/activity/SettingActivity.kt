package com.king250.kirafan.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.king250.kirafan.model.view.SettingView
import com.king250.kirafan.ui.page.SettingPage
import com.king250.kirafan.ui.theme.KiraraFansTheme
import kotlinx.coroutines.launch

class SettingActivity : ComponentActivity() {
    val s: SettingView by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            s.init()
        }
        setContent {
            KiraraFansTheme {
                SettingPage(this)
            }
        }
    }
}
