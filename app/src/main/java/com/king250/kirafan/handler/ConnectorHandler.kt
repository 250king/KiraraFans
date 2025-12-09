package com.king250.kirafan.handler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import com.king250.kirafan.Env
import com.king250.kirafan.ui.activity.MainActivity
import com.king250.kirafan.api
import com.king250.kirafan.model.data.Encrypted
import com.king250.kirafan.model.data.Session
import com.king250.kirafan.model.data.Endpoint
import com.king250.kirafan.model.data.Items
import com.king250.kirafan.service.ConnectorService
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.util.IpcUtil
import com.king250.kirafan.util.SecurityUtil
import go.Seq
import libv2ray.CoreCallbackHandler
import libv2ray.Libv2ray
import libv2ray.CoreController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.SoftReference

object ConnectorHandler {
    private var config = ""

    private val coreController: CoreController = Libv2ray.newCoreController(object : CoreCallbackHandler {
        override fun startup(): Long {
            return 0
        }

        override fun shutdown(): Long {
            val serviceControl = control?.get() ?: return -1
            return try {
                serviceControl.stopService()
                0
            }
            catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        }

        override fun onEmitStatus(l: Long, s: String?): Long {
            return 0
        }
    })

    private val receiver = object : BroadcastReceiver() {
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
            Libv2ray.initCoreEnv(
                value?.get()?.getService()?.getExternalFilesDir("assets")?.absolutePath,
                ClientUtil.getAndroidId(value?.get()?.getService()?.contentResolver!!)
            )
        }

    fun startVService(context: MainActivity) {
        if (coreController.isRunning) {
            return
        }
        api.protected.getEndpoints().enqueue(object : Callback<Items<Endpoint>> {
            override fun onResponse(call: Call<Items<Endpoint>>, response: Response<Items<Endpoint>>) {
                if (!response.isSuccessful) {
                    context.m.showSnackBar("服务器爆炸了！")
                    IpcUtil.toUI(context, Env.SERVICE_STOPPED)
                    return
                }
                context.m.setEndpoints(response.body()!!.items)
                if (context.m.selectedEndpoint.value > response.body()!!.total) {
                    context.m.setSelectedEndpoint(0)
                }
                val endpoint = response.body()!!.items[context.m.selectedEndpoint.value].region
                val key = SecurityUtil.getPublicKey() ?: return
                api.protected.createSession(Session(endpoint, key)).enqueue(object : Callback<Encrypted> {
                    override fun onResponse(call: Call<Encrypted?>, t: Response<Encrypted?>) {
                        if (!t.isSuccessful) {
                            when(t.code()) {
                                401 -> {
                                    context.logout()
                                }
                                403 -> {
                                    context.m.showSnackBar("你好像不在群组里，或者被ban了（")
                                }
                                else -> {
                                    context.m.showSnackBar("服务器出了点小差（")
                                }
                            }
                            IpcUtil.toUI(context, Env.SERVICE_STOPPED)
                            return
                        }
                        val data = t.body()!!
                        config = SecurityUtil.decrypt(data.key, data.iv, data.data) ?: return
                        val intent = Intent(context, ConnectorService::class.java)
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                            context.startForegroundService(intent)
                        }
                        else {
                            context.startService(intent)
                        }
                    }

                    override fun onFailure(call: Call<Encrypted?>, t: Throwable) {
                        IpcUtil.toUI(context, Env.SERVICE_STOPPED)
                    }
                })
            }

            override fun onFailure(call: Call<Items<Endpoint>>, t: Throwable) {
                IpcUtil.toUI(context, Env.SERVICE_STOPPED)
            }
        })
    }

    fun startCoreLoop(): Boolean {
        if (coreController.isRunning) {
            return false
        }
        val service = control?.get()?.getService() ?: return false
        IpcUtil.toUI(service, Env.SERVICE_STARTED)
        try {
            val filter = IntentFilter(Env.SERVICE_CHANNEL)
            ContextCompat.registerReceiver(service, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
            coreController.startLoop(config)
        }
        catch (e: Exception) {
            IpcUtil.toUI(service, Env.SERVICE_STOPPED)
            e.printStackTrace()
            return false
        }
        if (!coreController.isRunning) {
            IpcUtil.toUI(service, Env.SERVICE_STOPPED)
            return false
        }
        return true
    }

    fun stopCoreLoop(): Boolean {
        val service = control?.get()?.getService() ?: return false
        if (coreController.isRunning) {
            try {
                coreController.stopLoop()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        api.protected.revokeSession().enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit?>, response: Response<Unit?>) {}

            override fun onFailure(call: Call<Unit?>, t: Throwable) {}
        })
        try {
            service.unregisterReceiver(receiver)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        IpcUtil.toUI(service, Env.SERVICE_STOPPED)
        return true
    }
}