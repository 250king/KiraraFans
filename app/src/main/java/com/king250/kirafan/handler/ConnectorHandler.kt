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
import com.king250.kirafan.model.data.ChangeOperation
import com.king250.kirafan.model.data.Endpoint
import com.king250.kirafan.service.ConnectorService
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.util.IpcUtil
import go.Seq
import libv2ray.CoreCallbackHandler
import libv2ray.Libv2ray
import libv2ray.CoreController
import okhttp3.ResponseBody
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
        api.protected.getEndpoints().enqueue(object : Callback<List<Endpoint>> {
            override fun onResponse(call: Call<List<Endpoint>>, response: Response<List<Endpoint>>) {
                context.m.setEndpoints(response.body()!!)
                if (context.m.selectedEndpoint.value > response.body()!!.count()) {
                    context.m.setSelectedEndpoint(0)
                }
                val endpoint = response.body()!![context.m.selectedEndpoint.value].region
                api.protected.changeEndpoint(ChangeOperation(endpoint)).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody?>, t: Response<ResponseBody?>) {
                        config = t.body()!!.string()
                        val intent = Intent(context, ConnectorService::class.java)
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                            context.startForegroundService(intent)
                        }
                        else {
                            context.startService(intent)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                        IpcUtil.toUI(context, Env.SERVICE_STOPPED)
                    }
                })
            }

            override fun onFailure(call: Call<List<Endpoint>>, t: Throwable) {
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
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.RECEIVER_EXPORTED
            }
            else {
                ContextCompat.RECEIVER_NOT_EXPORTED
            }
            ContextCompat.registerReceiver(service, receiver, filter, flags)
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