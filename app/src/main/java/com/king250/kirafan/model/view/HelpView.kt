package com.king250.kirafan.model.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.king250.kirafan.api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelpView(application: Application) : AndroidViewModel(application) {
    private val _loading = MutableStateFlow(true)

    private val _refresh = MutableStateFlow(false)

    private val _content = MutableStateFlow("")

    val loading: StateFlow<Boolean> = _loading

    val refresh: StateFlow<Boolean> = _refresh

    val content: StateFlow<String> = _content

    fun fetch() {
        api.protected.getHelp().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(p0: Call<ResponseBody?>, p1: Response<ResponseBody?>) {
                _content.value = p1.body()?.string() ?: ""
                _loading.value = false
                _refresh.value = false
            }

            override fun onFailure(p0: Call<ResponseBody?>, p1: Throwable) {
                p1.printStackTrace()
                _loading.value = false
                _refresh.value = false
            }
        })
    }

    fun setRefresh(refresh: Boolean) {
        _refresh.value = refresh
    }
}