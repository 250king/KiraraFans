package com.king250.kirafan.model.view

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.gson.JsonParser
import com.king250.kirafan.Env
import com.king250.kirafan.model.data.UserItem
import com.king250.kirafan.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
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
            val context = getApplication<Application>().applicationContext
            when (p1?.getIntExtra("action", -1)) {
                Env.SERVICE_STARTED -> {
                    _isConnect.value = true
                    Toast.makeText(context, "已连接至专网", Toast.LENGTH_SHORT).show()
                }
                Env.SERVICE_STOPPED -> {
                    _isConnect.value = false
                    Toast.makeText(context, "已断开连接", Toast.LENGTH_SHORT).show()
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
                if (!file.exists()) {
                    val output = FileOutputStream(file)
                    input.copyTo(output)
                    input.close()
                    output.close()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(handler, filter, Context.RECEIVER_EXPORTED)
        }
        else {
            getApplication<Application>().registerReceiver(handler, filter)
        }
    }

    suspend fun fetch(token: String) {
        try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("${Env.QLOGIN_API}/me")
                    .header("Authorization", "Bearer $token")
                    .build()
                val response = Utils.httpClient.newCall(request).execute()
                if (response.code == 200) {
                    val result = JsonParser.parseString(response.body?.string()).asJsonObject
                    response.close()
                    _user.value = UserItem(
                        result.get("name").asString,
                        result.get("avatar").asString
                    )
                }
            }
        }
        catch (e: Exception) {
            val context = getApplication<Application>().applicationContext
            e.printStackTrace()
            Toast.makeText(context, "网络好像不太好~", Toast.LENGTH_LONG).show()
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