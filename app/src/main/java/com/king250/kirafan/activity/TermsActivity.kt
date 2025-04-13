package com.king250.kirafan.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.king250.kirafan.model.view.TermsView
import com.king250.kirafan.ui.page.TermsPage
import com.king250.kirafan.ui.theme.KiraraFansTheme

class TermsActivity : ComponentActivity() {
    val t: TermsView by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiraraFansTheme {
                TermsPage(this)
            }
        }
        t.fetch()
    }
}
