package com.king250.kirafan.model.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.king250.kirafan.api.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HelpView(application: Application) : AndroidViewModel(application) {
    private val _loading = MutableStateFlow(true)

    private val _refresh = MutableStateFlow(false)

    private val _content = MutableStateFlow("")

    val loading: StateFlow<Boolean> = _loading

    val refresh: StateFlow<Boolean> = _refresh

    val content: StateFlow<String> = _content

    fun fetch() {
        viewModelScope.launch {
            try {
                _content.value = withContext(Dispatchers.IO) {
                    Api.kirara.getArticle("help")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
                _refresh.value = false
            }
        }
    }

    fun setRefresh(refresh: Boolean) {
        _refresh.value = refresh
    }
}