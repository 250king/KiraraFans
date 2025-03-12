package com.king250.kirafan.model.view

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.material3.SnackbarHostState
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.king250.kirafan.BuildConfig
import com.king250.kirafan.Env
import com.king250.kirafan.model.data.Endpoint
import com.king250.kirafan.model.data.Release
import com.king250.kirafan.model.data.User
import com.king250.kirafan.util.HttpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class MainView(application: Application) : AndroidViewModel(application) {
    private var snackBarHostState: SnackbarHostState? = null

    private val _user = MutableStateFlow<User?>(null)

    private val _version = MutableStateFlow<String?>(null)

    private val _endpoints = MutableStateFlow(emptyList<Endpoint>())

    private val _selectedEndpoint = MutableStateFlow(0)

    private val _isDisabledConnect = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(true)

    private val _isConnected = MutableStateFlow(false)

    private val _isUnsupported = MutableStateFlow(false)

    private val _openRoot = MutableStateFlow(false)

    private val _openUsb = MutableStateFlow(false)

    private val _openVersion = MutableStateFlow(false)

    private val _openUpdate = MutableStateFlow(false)

    private val _openSelect = MutableStateFlow(false)

    val user: StateFlow<User?> = _user

    val version: StateFlow<String?> = _version

    val endpoints: StateFlow<List<Endpoint>> = _endpoints

    val selectedEndpoint: StateFlow<Int> = _selectedEndpoint

    val isDisabledConnect: StateFlow<Boolean> = _isDisabledConnect

    val isLoading: StateFlow<Boolean> = _isLoading

    val isConnected: StateFlow<Boolean> = _isConnected

    val isUnsupported: StateFlow<Boolean> = _isUnsupported

    val openRoot: StateFlow<Boolean> = _openRoot

    val openUsb: StateFlow<Boolean> = _openUsb

    val openVersion: StateFlow<Boolean> = _openVersion

    val openUpdate: StateFlow<Boolean> = _openUpdate

    val openSelect: StateFlow<Boolean> = _openSelect

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
        ContextCompat.registerReceiver(
            getApplication(),
            handler,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun refresh() {
        HttpUtil.protected.getProfile().enqueue(object : Callback<User> {
            override fun onResponse(p0: Call<User>, p1: Response<User>) {
                _user.value = p1.body()
                _isLoading.value = false
            }

            override fun onFailure(p0: Call<User>, p1: Throwable) {
                p1.printStackTrace()
                _isLoading.value = false
            }
        })
    }

    fun check() {
        HttpUtil.public.getRelease().enqueue(object : Callback<Release> {
            override fun onResponse(p0: Call<Release>, p1: Response<Release>) {
                if (!p1.isSuccessful) {
                    showSnackBar("无法获得最新版本状态（")
                    return
                }
                val release = p1.body()!!.version.split(".").map { it.toInt() }
                val app = BuildConfig.VERSION_NAME.split(".").map { it.toInt() }
                app.forEachIndexed { index, number ->
                    if (release[index] > number) {
                        _openUpdate.value = true
                    }
                }
            }

            override fun onFailure(p0: Call<Release>, p1: Throwable) {
                p1.printStackTrace()
                showSnackBar("网络好像不太好哦~")
            }
        })
    }

    fun setUser(value: User?) {
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
        _openRoot.value = value
    }

    fun setIsUsb(value: Boolean) {
        _openUsb.value = value
    }

    fun setIsVersionBad(value: Boolean) {
        _openVersion.value = value
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

    fun setEndpoints(value: List<Endpoint>) {
        _endpoints.value = value
    }

    fun setSelectedEndpoint(value: Int) {
        _selectedEndpoint.value = value
    }

    fun setOpenSelect(value: Boolean) {
        _openSelect.value = value
    }
}
