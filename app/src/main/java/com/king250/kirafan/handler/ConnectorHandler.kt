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
                Env.STOP_SERVICE, Env.START_FAILED -> {
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
        a.v.showSnackBar("启动中……")
        HttpUtil.protected.getEndpoints().enqueue(object : Callback<List<Endpoint>> {
            override fun onResponse(p0: Call<List<Endpoint>>, p1: Response<List<Endpoint>>) {
                a.v.setEndpoints(p1.body() ?: emptyList())
                if (a.v.endpoints.value.isEmpty()) {
                    a.v.showSnackBar("没有可用节点！")
                    return
                }
                if (a.v.selectedEndpoint.value >= a.v.endpoints.value.size) {
                    a.v.setSelectedEndpoint(0)
                }
                val payload = ChangeOperation(a.v.endpoints.value[a.v.selectedEndpoint.value].region)
                HttpUtil.protected.changeEndpoint(payload).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(p0: Call<ResponseBody>, p1: Response<ResponseBody>) {
                        when (p1.code()) {
                            200 -> {
                                config = p1.body()?.string() ?: ""
                                host = "${a.v.endpoints.value[a.v.selectedEndpoint.value].region}.tunnel.kirafan.xyz:443"
                                val intent = Intent(a, ConnectorService::class.java)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    a.startForegroundService(intent)
                                }
                                else {
                                    a.startService(intent)
                                }
                            }
                            403 -> {
                                a.logout()
                            }
                            else -> {
                                a.v.showSnackBar("服务器炸了！")
                            }
                        }
                        if (p1.code() != 200) {
                            val intent = Intent()
                            intent.action = Env.UI_CHANNEL
                            intent.putExtra("action", Env.START_FAILED)
                            a.sendBroadcast(intent)
                        }
                    }

                    override fun onFailure(p0: Call<ResponseBody>, p1: Throwable) {
                        p1.printStackTrace()
                        a.v.showSnackBar("网络好像不太好哦~")
                        val intent = Intent()
                        intent.action = Env.UI_CHANNEL
                        intent.putExtra("action", Env.START_FAILED)
                        a.sendBroadcast(intent)
                    }
                })
            }

            override fun onFailure(p0: Call<List<Endpoint>>, p1: Throwable) {
                p1.printStackTrace()
                a.v.showSnackBar("网络好像不太好哦~")
                val intent = Intent()
                intent.action = Env.UI_CHANNEL
                intent.putExtra("action", Env.START_FAILED)
                a.sendBroadcast(intent)
            }
        })
    }

    fun startV2RayPoint() {
        val service = control?.get()?.getService() ?: return
        if (v2RayPoint.isRunning || config.isEmpty()) {
            return
        }
        val filter = IntentFilter(Env.SERVICE_CHANNEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            service.registerReceiver(handler, filter, Context.RECEIVER_NOT_EXPORTED)
        }
        else {
            ContextCompat.registerReceiver(service, handler, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
        v2RayPoint.configureFileContent = config
        v2RayPoint.domainName = host
        val intent = Intent()
        intent.action = Env.UI_CHANNEL
        try {
            v2RayPoint.runLoop(false)
            intent.putExtra("action", Env.SERVICE_STARTED)
        }
        catch (e: Exception) {
            e.printStackTrace()
            v2RayPoint.stopLoop()
            intent.putExtra("action", Env.SERVICE_STOPPED)
        }
        service.sendBroadcast(intent)
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
        val intent = Intent()
        intent.action = Env.UI_CHANNEL
        intent.putExtra("action", Env.SERVICE_STOPPED)
        service.sendBroadcast(intent)
        service.unregisterReceiver(handler)
        HttpUtil.protected.revokeSession().enqueue(object : Callback<Unit> {
            override fun onResponse(p0: Call<Unit>, p1: Response<Unit>) {}

            override fun onFailure(p0: Call<Unit>, p1: Throwable) {
                p1.printStackTrace()
            }
        })
    }
}