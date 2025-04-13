package com.king250.kirafan.handler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import com.king250.kirafan.Env
import com.king250.kirafan.activity.MainActivity
import com.king250.kirafan.model.data.ChangeOperation
import com.king250.kirafan.model.data.Endpoint
import com.king250.kirafan.service.ConnectorService
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.util.HttpUtil
import com.king250.kirafan.util.IpcUtil
import go.Seq
import libv2ray.Libv2ray
import libv2ray.V2RayPoint
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.SoftReference

object ConnectorHandler {
    private val v2RayPoint: V2RayPoint = Libv2ray.newV2RayPoint(V2RayHandler(), true)

    private var config = ""

    private var host = ""

    private val handler = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val serviceControl = control?.get() ?: return
            when (p1?.getIntExtra("action", -1)) {
                Env.STOP_SERVICE -> {
                    serviceControl.stopService()
                }
                else -> {}
            }
        }
    }

    var control: SoftReference<ServiceHandler>? = null
        set(value) {
            field = value
            Seq.setContext(value?.get()?.getService()?.application)
            Libv2ray.initV2Env(
                value?.get()?.getService()?.getExternalFilesDir("assets")?.absolutePath,
                ClientUtil.getAndroidId(value?.get()?.getService()?.contentResolver!!)
            )
        }

    fun startV2Ray(a: MainActivity) {
        if (v2RayPoint.isRunning) {
            return
        }
        HttpUtil.protected.getEndpoints().enqueue(object : Callback<List<Endpoint>> {
            override fun onResponse(p0: Call<List<Endpoint>>, p1: Response<List<Endpoint>>) {
                a.s.setEndpoints(p1.body() ?: emptyList())
                if (a.s.endpoints.value.isEmpty()) {
                    a.s.showSnackBar("没有可用节点！")
                    IpcUtil.toUI(a, Env.SERVICE_STOPPED)
                    return
                }
                if (a.s.selectedEndpoint.value >= a.s.endpoints.value.size) {
                    a.s.setSelectedEndpoint(0)
                }
                val region = a.s.endpoints.value[a.s.selectedEndpoint.value].region
                val payload = ChangeOperation(region)
                HttpUtil.protected.changeEndpoint(payload).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(p0: Call<ResponseBody>, p1: Response<ResponseBody>) {
                        when (p1.code()) {
                            200 -> {
                                val result = p1.body()?.string()
                                if (result.isNullOrEmpty()) {
                                    a.s.showSnackBar("配置文件为空")
                                    IpcUtil.toUI(a, Env.SERVICE_STOPPED)
                                    return
                                }
                                config = result
                                host = "${region}.tunnel.kirafan.xyz:443"
                                val intent = Intent(a, ConnectorService::class.java)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    a.startForegroundService(intent)
                                }
                                else {
                                    a.startService(intent)
                                }
                            }
                            403 -> {
                                a.s.showSnackBar("凭证已失效，请稍后再试")
                            }
                            in 500..599 -> {
                                a.s.showSnackBar("服务器炸了！")
                            }
                        }
                        if (p1.code() != 200) {
                            IpcUtil.toUI(a, Env.SERVICE_STOPPED)
                        }
                    }

                    override fun onFailure(p0: Call<ResponseBody>, p1: Throwable) {
                        p1.printStackTrace()
                        a.s.showSnackBar("网络好像不太好哦~")
                        IpcUtil.toUI(a, Env.SERVICE_STOPPED)
                    }
                })
            }

            override fun onFailure(p0: Call<List<Endpoint>>, p1: Throwable) {
                p1.printStackTrace()
                a.s.showSnackBar("网络好像不太好哦~")
                IpcUtil.toUI(a, Env.SERVICE_STOPPED)
            }
        })
    }

    fun startV2RayPoint() {
        val service = control?.get()?.getService() ?: return
        if (v2RayPoint.isRunning || config.isEmpty()) {
            return
        }
        val filter = IntentFilter(Env.SERVICE_CHANNEL)
        ContextCompat.registerReceiver(service, handler, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        v2RayPoint.configureFileContent = config
        v2RayPoint.domainName = host
        try {
            v2RayPoint.runLoop(false)
            IpcUtil.toUI(service.applicationContext, Env.SERVICE_STARTED)
        }
        catch (e: Exception) {
            e.printStackTrace()
            control?.get()?.stopService()
        }
    }

    fun stopV2rayPoint() {
        val service = control?.get()?.getService() ?: return
        if (v2RayPoint.isRunning) {
            try {
                v2RayPoint.stopLoop()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        IpcUtil.toUI(service, Env.SERVICE_STOPPED)
        service.unregisterReceiver(handler)
        HttpUtil.protected.revokeSession().enqueue(object : Callback<Unit> {
            override fun onResponse(p0: Call<Unit>, p1: Response<Unit>) {}

            override fun onFailure(p0: Call<Unit>, p1: Throwable) {
                p1.printStackTrace()
            }
        })
    }
}