package com.king250.kirafan

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.king250.kirafan.api.HttpApi

val api: HttpApi
    get() = HttpApi

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("main")

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
