package com.king250.kirafan.model.view

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.alibaba.fastjson2.JSON
import com.king250.kirafan.Env
import com.king250.kirafan.model.data.UserItem
import com.king250.kirafan.util.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream


@SuppressLint("UnspecifiedRegisterReceiverFlag")
class MainState(application: Application) : AndroidViewModel(application) {
    private val _user = MutableStateFlow<UserItem?>(null)

    private val _version = MutableStateFlow<String?>(null)

    private val _isDisableLogin = MutableStateFlow(true)

    private val _isDisableDownload = MutableStateFlow(false)

    private val _isConnect = MutableStateFlow(false)

    private val handler = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1?.getIntExtra("action", -1)) {
                Env.SERVICE_STARTED -> {
                    _isConnect.value = true
                }
                Env.SERVICE_STOPPED -> {
                    _isConnect.value = false
                }
                else -> {}
            }
        }
    }

    val user: StateFlow<UserItem?> = _user

    val version: StateFlow<String?> = _version

    val isDisableLogin: StateFlow<Boolean> = _isDisableLogin

    val isDisableDownload: StateFlow<Boolean> = _isDisableDownload

    val isConnect: StateFlow<Boolean> = _isConnect

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(handler)
    }

    fun init() {
        val filter = IntentFilter(Env.UI_CHANNEL)
        val assets = getApplication<Application>().applicationContext.assets
        val dir = getApplication<Application>().getExternalFilesDir("assets")?.absolutePath
        assets.list("")?.forEach {
            if (it.endsWith(".dat")) {
                val input = assets.open(it)
                val file = File(dir, it)
                val output = FileOutputStream(file)
                input.copyTo(output)
                input.close()
                output.close()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(handler, filter, Context.RECEIVER_EXPORTED)
        }
        else {
            getApplication<Application>().registerReceiver(handler, filter)
        }
    }

    fun fetch(token: String) {
        val request = Request.Builder()
            .url("${Env.QLOGIN_API}/me")
            .header("Authorization", "Bearer $token")
            .build()
        val response = Utils.httpClient.newCall(request).execute()
        if (response.code == 200) {
            val result = JSON.parseObject(response.body?.string())
            response.close()
            _user.value = UserItem(
                result.getString("name"),
                result.getString("avatar")
            )
        }
        _isDisableLogin.value = false
    }

    fun login(user: UserItem?) {
        _user.value = user
    }

    fun update(version: String?) {
        _version.value = version
    }

    fun disableLogin(state: Boolean) {
        _isDisableLogin.value = state
    }

    fun disableDownload(state: Boolean) {
        _isDisableDownload.value = state
    }
}