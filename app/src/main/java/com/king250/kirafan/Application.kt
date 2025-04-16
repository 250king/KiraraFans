package com.king250.kirafan

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.king250.kirafan.api.HttpApi

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("main")

val Context.api: HttpApi
    get() = HttpApi

class Application : Application() {
    companion object {
        lateinit var application: Application
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        HttpApi.init(this)
    }
}
