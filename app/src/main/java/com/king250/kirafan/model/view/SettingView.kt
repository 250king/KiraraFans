package com.king250.kirafan.model.view

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import com.king250.kirafan.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class SettingView(application: Application) : AndroidViewModel(application) {
    private val _dns = MutableStateFlow(false)

    val dns: StateFlow<Boolean> = _dns

    suspend fun init() {
        val context = getApplication<Application>().applicationContext
        _dns.value = context.dataStore.data.map{it[booleanPreferencesKey("dns")]}.firstOrNull() ?: false
    }

    suspend fun setDns(value: Boolean) {
        _dns.value = value
        val context = getApplication<Application>().applicationContext
        context.dataStore.edit {
            it[booleanPreferencesKey("dns")] = value
        }
    }
}
