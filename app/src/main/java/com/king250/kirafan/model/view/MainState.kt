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
import androidx.compose.material3.SnackbarHostState
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import com.king250.kirafan.BuildConfig
import com.king250.kirafan.Env
import com.king250.kirafan.model.data.UserItem
import com.king250.kirafan.util.ClientUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class MainState(application: Application) : AndroidViewModel(application) {
    private var snackBarHostState: SnackbarHostState? = null

    private val _user = MutableStateFlow<UserItem?>(null)

    private val _version = MutableStateFlow<String?>(null)

    private val _isDisabledConnect = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(true)

    private val _isConnected = MutableStateFlow(false)

    private val _isUnsupported = MutableStateFlow(false)

    private val _isRoot = MutableStateFlow(false)

    private val _isUsb = MutableStateFlow(false)

    private val _isBadVersion = MutableStateFlow(false)

    private val _isNeedUpdate = MutableStateFlow(false)

    val user: StateFlow<UserItem?> = _user

    val version: StateFlow<String?> = _version

    val isDisabledConnect: StateFlow<Boolean> = _isDisabledConnect

    val isLoading: StateFlow<Boolean> = _isLoading

    val isConnected: StateFlow<Boolean> = _isConnected

    val isUnsupported: StateFlow<Boolean> = _isUnsupported

    val isRoot: StateFlow<Boolean> = _isRoot

    val isUsb: StateFlow<Boolean> = _isUsb

    val isBadVersion: StateFlow<Boolean> = _isBadVersion

    val isNeedUpdate: StateFlow<Boolean> = _isNeedUpdate

    private val handler = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1?.getIntExtra("action", -1)) {
                Env.SERVICE_STARTED -> {
                    _isConnected.value = true
                }
                Env.SERVICE_STOPPED -> {
                    _isConnected.value = false
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
            if (it.endsWith(".dat") || it.endsWith(".yaml")) {
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
            val notificationManager = getSystemService(context, NotificationManager::class.java)
            val channel = NotificationChannel(
                Env.NOTIFICATION_CHANNEL,
                "GNet™ VPN Gateway Connector",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "显示连接状态"
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

    suspend fun refresh() {
        val context = getApplication<Application>().applicationContext
        try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url("${Env.SERVER_API}/1.0/me").build()
                val response = ClientUtil.http(context, true).newCall(request).execute()
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
        val context = getApplication<Application>().applicationContext
        try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(Env.RELEASE_API).build()
                val response = ClientUtil.http(context).newCall(request).execute()
                if (response.code == 200) {
                    val result = JsonParser.parseString(response.body?.string()).asJsonObject
                    if (result.get("tag_name").asString != BuildConfig.VERSION_NAME) {
                        _isNeedUpdate.value = true
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        showSnackBar("无法获得最新版本状态（")
                    }
                }
                response.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showSnackBar("网络好像不太好哦~")
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

    fun setIsUnsupported(value: Boolean) {
        _isUnsupported.value = value
    }

    fun setIsRoot(value: Boolean) {
        _isRoot.value = value
    }

    fun setIsUsb(value: Boolean) {
        _isUsb.value = value
    }

    fun setIsVersionBad(value: Boolean) {
        _isBadVersion.value = value
    }

    fun setSnackBarHostState(value: SnackbarHostState) {
        snackBarHostState = value
    }

    fun showSnackBar(message: String) {
        snackBarHostState?.let {
            viewModelScope.launch {
                it.showSnackbar(
                    message = message
                )
            }
        }
    }
}
