package com.king250.kirafan.model.view

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.king250.kirafan.Env
import com.king250.kirafan.util.UserAgentInterceptor
import com.king250.kirafan.util.Utils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.sse.RealEventSource
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import java.util.concurrent.TimeUnit

class LoginState(application: Application) : AndroidViewModel(application) {
    private lateinit var handler: RealEventSource

    private val mediaType = "application/json".toMediaType()

    private var isCanceled = false

    private val _code = MutableStateFlow("(☆ω☆)")

    private val _time = MutableStateFlow(0f)

    private val _anime = MutableStateFlow(0)

    private val _activate = MutableStateFlow(false)

    private val _token = MutableSharedFlow<String>()

    val code: StateFlow<String> = _code

    val time: StateFlow<Float> = _time

    val anime: StateFlow<Int> = _anime

    val activate: StateFlow<Boolean> = _activate

    val token: SharedFlow<String> = _token

    private val json = JsonObject().apply {
        addProperty("device_id", Utils.getDID(application.contentResolver))
    }

    private val requestBody = json.toString().toRequestBody(mediaType)

    private val request = Request
        .Builder()
        .url("${Env.QLOGIN_API}/auth/session")
        .post(requestBody)
        .build()

    fun start() {
        val client = OkHttpClient
            .Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.MINUTES)
            .addInterceptor(UserAgentInterceptor)
            .build()
        handler = RealEventSource(request, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                super.onEvent(eventSource, id, type, data)
                when (type) {
                    "code" -> {
                        val params = data.split(";")
                        _code.value = params[1]
                        _anime.value = 0
                        _time.value = (30 - params[0].toInt() % 30) / 30f
                    }
                    "login" -> {
                        viewModelScope.launch {
                            _token.emit(data)
                        }
                        stop()
                    }
                    "ping" -> {
                        _anime.value = 1000
                        _time.value = (30 - data.toInt() % 30) / 30f
                    }
                    else -> {}
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                super.onFailure(eventSource, t, response)
                if (!isCanceled) {
                    val context = getApplication<Application>().applicationContext
                    t?.printStackTrace()
                    _activate.value = false
                    _anime.value = 0
                    _code.value = "(╯︵╰,)"
                    _time.value = 0f
                    viewModelScope.launch {
                        Toast.makeText(context, "网络好像不太好，退出再试试吧~", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onOpen(eventSource: EventSource, response: Response) {
                super.onOpen(eventSource, response)
                _activate.value = true
            }

            override fun onClosed(eventSource: EventSource) {
                super.onClosed(eventSource)
                _activate.value = false
            }
        })
        isCanceled = false
        handler.connect(client)
    }

    fun stop() {
        isCanceled = true
        handler.cancel()
    }
}