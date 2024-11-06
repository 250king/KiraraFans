package com.king250.kirafan.model.view

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import com.google.gson.JsonParser
import com.king250.kirafan.BuildConfig
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

    private val _isDisabledConnect = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(true)

    private val _isConnected = MutableStateFlow(false)

    val user: StateFlow<UserItem?> = _user

    val version: StateFlow<String?> = _version

    val isDisabledConnect: StateFlow<Boolean> = _isDisabledConnect

    val isLoading: StateFlow<Boolean> = _isLoading

    val isConnected: StateFlow<Boolean> = _isConnected

    private val handler = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val context = getApplication<Application>().applicationContext
            when (p1?.getIntExtra("action", -1)) {
                Env.SERVICE_STARTED -> {
                    _isConnected.value = true
                    Toast.makeText(context, "已连接至专网", Toast.LENGTH_SHORT).show()
                }

                Env.SERVICE_STOPPED -> {
                    _isConnected.value = false
                    Toast.makeText(context, "已断开连接", Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
            _isDisabledConnect.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(handler)
    }

    suspend fun init() {
        val filter = IntentFilter(Env.UI_CHANNEL)
        val assets = getApplication<Application>().applicationContext.assets
        val dir = getApplication<Application>().getExternalFilesDir("assets")?.absolutePath
        assets.list("")?.forEach {
            if (it.endsWith(".dat")) {
                val input = assets.open(it)
                val file = File(dir, it)
                if (!file.exists()) {
                    withContext(Dispatchers.IO) {
                        val output = FileOutputStream(file)
                        input.copyTo(output)
                        input.close()
                        output.close()
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val context = getApplication<Application>().applicationContext
            val channel = NotificationChannel(
                Env.NOTIFICATION_CHANNEL,
                "GNet™ VPN Gateway状态",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "提示专网连接状态"
            }
            val notificationManager = getSystemService(context, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(
                handler,
                filter,
                Context.RECEIVER_EXPORTED
            )
        }
        else {
            getApplication<Application>().registerReceiver(handler, filter)
        }
    }

    suspend fun fetch(context: Context) {
        try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url("${Env.SERVER_API}/1.0/me").build()
                val response = Utils.http(context).newCall(request).execute()
                if (response.code == 200) {
                    val result = JsonParser.parseString(response.body?.string()).asJsonObject
                    _user.value = UserItem(
                        result.get("name").asString,
                        result.get("avatar").asString
                    )
                }
                response.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isLoading.value = false
    }

    suspend fun check() {
        try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(Env.RELEASE_API).build()
                val response = Utils.http().newCall(request).execute()
                if (response.code == 200) {
                    val result = JsonParser.parseString(response.body?.string()).asJsonObject
                    if (result.get("tag_name").asString != BuildConfig.VERSION_NAME) {
                        withContext(Dispatchers.Main) {
                            val context = getApplication<Application>().applicationContext
                            Toast.makeText(context, "发现新版本了！", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        val context = getApplication<Application>().applicationContext
                        Toast.makeText(context, "无法获得最新版本状态（", Toast.LENGTH_LONG).show()
                    }
                }
                response.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val context = getApplication<Application>().applicationContext
            Toast.makeText(context, "网络好像不太好~", Toast.LENGTH_LONG).show()
        }
    }

    fun setUser(value: UserItem?) {
        _user.value = value
    }

    fun setVersion(value: String?) {
        _version.value = value
    }

    fun setIsDisabledConnect(value: Boolean) {
        _isDisabledConnect.value = value
    }

    fun setIsLoading(value: Boolean) {
        _isLoading.value = value
    }
}
