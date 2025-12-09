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
import com.king250.kirafan.api
import com.king250.kirafan.BuildConfig
import com.king250.kirafan.Env
import com.king250.kirafan.model.data.Endpoint
import com.king250.kirafan.model.data.Release
import com.king250.kirafan.model.data.User
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

    private val _disabledConnect = MutableStateFlow(false)

    private val _loading = MutableStateFlow(true)

    private val _connected = MutableStateFlow(false)

    private val _update = MutableStateFlow(false)

    val user: StateFlow<User?> = _user

    val version: StateFlow<String?> = _version

    val endpoints: StateFlow<List<Endpoint>> = _endpoints

    val selectedEndpoint: StateFlow<Int> = _selectedEndpoint

    val disabledConnect: StateFlow<Boolean> = _disabledConnect

    val loading: StateFlow<Boolean> = _loading

    val connected: StateFlow<Boolean> = _connected

    val update: StateFlow<Boolean> = _update

    private val handler = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1?.getIntExtra("action", -1)) {
                Env.SERVICE_STARTED -> {
                    _connected.value = true
                }
                Env.SERVICE_STOPPED -> {
                    _connected.value = false
                }
                else -> {}
            }
            _disabledConnect.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(handler)
    }

    suspend fun init() {
        val filter = IntentFilter(Env.UI_CHANNEL)
        val context = getApplication<Application>()
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
            val notificationManager = getSystemService(context, NotificationManager::class.java)
            val channel = NotificationChannel(
                Env.NOTIFICATION_CHANNEL,
                "GNet™ VPN Gateway Connector",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "显示连接状态"
            notificationManager?.createNotificationChannel(channel)
        }
        ContextCompat.registerReceiver(context, handler, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun refresh() {
        api.protected.getProfile().enqueue(object : Callback<User> {
            override fun onResponse(p0: Call<User>, p1: Response<User>) {
                _user.value = p1.body()
                _loading.value = false
            }

            override fun onFailure(p0: Call<User>, p1: Throwable) {
                p1.printStackTrace()
                _loading.value = false
            }
        })
    }

    fun check() {
        api.public.getRelease().enqueue(object : Callback<Release> {
            override fun onResponse(p0: Call<Release>, p1: Response<Release>) {
                if (!p1.isSuccessful) {
                    showSnackBar("无法获得最新版本状态（")
                    return
                }
                val release = p1.body()!!.code
                val app = BuildConfig.VERSION_CODE
                if (release > app) {
                    _update.value = true
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

    fun setDisabledConnect(value: Boolean) {
        _disabledConnect.value = value
    }

    fun setEndpoints(value: List<Endpoint>) {
        _endpoints.value = value
    }

    fun setSelectedEndpoint(value: Int) {
        _selectedEndpoint.value = value
    }

    fun setLoading(value: Boolean) {
        _loading.value = value
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
