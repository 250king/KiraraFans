package com.king250.kirafan.model.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.king250.kirafan.Env
import com.king250.kirafan.util.UserAgentInterceptor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.sse.RealEventSource
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import java.util.concurrent.TimeUnit

class LoginState(application: Application) : AndroidViewModel(application) {
    private val _code = MutableStateFlow("")

    private val _token = MutableSharedFlow<String>()

    val code: StateFlow<String> = _code

    val token: SharedFlow<String> = _token

    private val request = Request.Builder().url("${Env.QLOGIN_API}/auth/session").build()

    private val handler = RealEventSource(request, object : EventSourceListener() {
        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            super.onEvent(eventSource, id, type, data)
            when (type) {
                "code" -> {
                    _code.value = data
                }
                "login" -> {
                    eventSource.cancel()
                    viewModelScope.launch {
                        _token.emit(data)
                    }
                }
                else -> {}
            }
        }
    })

    fun start() {
        val client = OkHttpClient
            .Builder()
            .readTimeout(1, TimeUnit.DAYS)
            .connectTimeout(1, TimeUnit.DAYS)
            .addInterceptor(UserAgentInterceptor)
            .build()
        handler.connect(client)
    }

    fun stop() {
        handler.cancel()
    }
}