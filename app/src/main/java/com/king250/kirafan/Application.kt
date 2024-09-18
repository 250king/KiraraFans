package com.king250.kirafan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.multidex.MultiDexApplication

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("main")

class Application : MultiDexApplication() {
    companion object {
        lateinit var application: Application
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }
}