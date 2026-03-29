package com.king250.kirafan.handler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.content.ContextCompat
import com.king250.kirafan.Env
import com.king250.kirafan.ui.activity.MainActivity
import com.king250.kirafan.api.Api
import com.king250.kirafan.model.data.Session
import com.king250.kirafan.service.ConnectorService
import com.king250.kirafan.util.ClientUtil
import com.king250.kirafan.util.IpcUtil
import com.king250.kirafan.util.SecurityUtil
import go.Seq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import libv2ray.CoreCallbackHandler
import libv2ray.Libv2ray
import libv2ray.CoreController
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
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val res = Api.kirara.getEndpoints()
                if (res.items.isEmpty()) {
                    IpcUtil.toUI(context, Env.SERVICE_STOPPED)
                    context.m.showSnackBar("没有可用的服务器（")
                    return@launch
                }
                context.m.setEndpoints(res.items)
                if (context.m.selectedEndpoint.value > res.total) {
                    context.m.setSelectedEndpoint(0)
                }
                val endpoint = res.items[context.m.selectedEndpoint.value].region
                val key = SecurityUtil.getPublicKey() ?: return@launch
                val session = Api.kirara.createSession(Session(endpoint, key))
                config = SecurityUtil.decrypt(session.key, session.iv, session.data) ?: return@launch
                val intent = Intent(context, ConnectorService::class.java)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                    context.startForegroundService(intent)
                }
                else {
                    context.startService(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            context.m.showSnackBar("连接失败了（")
            IpcUtil.toUI(context, Env.SERVICE_STOPPED)
            return
        }
    }

    fun startCoreLoop(fd: ParcelFileDescriptor): Boolean {
        if (coreController.isRunning) {
            return false
        }
        val service = control?.get()?.getService() ?: return false
        IpcUtil.toUI(service, Env.SERVICE_STARTED)
        try {
            val filter = IntentFilter(Env.SERVICE_CHANNEL)
            ContextCompat.registerReceiver(service, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
            coreController.startLoop(config, fd.fd)
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
        try {
            CoroutineScope(Dispatchers.IO).launch {
                Api.kirara.revokeSession()
            }
            service.unregisterReceiver(receiver)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        IpcUtil.toUI(service, Env.SERVICE_STOPPED)
        return true
    }
}