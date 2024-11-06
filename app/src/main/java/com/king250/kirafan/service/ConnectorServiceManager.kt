package com.king250.kirafan.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import com.king250.kirafan.Env
import com.king250.kirafan.util.Utils
import go.Seq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import libv2ray.Libv2ray
import libv2ray.V2RayPoint
import libv2ray.V2RayVPNServiceSupportsSet
import okhttp3.Request
import java.lang.ref.SoftReference

class V2RayCallback : V2RayVPNServiceSupportsSet {
    override fun shutdown(): Long {
        val serviceControl = ConnectorServiceManager.serviceControl?.get() ?: return -1
        return try {
            serviceControl.stopService()
            1
        }
        catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    override fun prepare(): Long {
        return 0
    }

    override fun protect(p0: Long): Boolean {
        val serviceControl = ConnectorServiceManager.serviceControl?.get() ?: return true
        return serviceControl.vpnProtect(p0.toInt())
    }

    override fun onEmitStatus(p0: Long, p1: String?): Long {
        return 0
    }

    override fun setup(p0: String?): Long {
        val serviceControl = ConnectorServiceManager.serviceControl?.get() ?: return -1
        return try {
            serviceControl.startService()
            0
        }
        catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
}

class Handler: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val serviceControl = ConnectorServiceManager.serviceControl?.get() ?: return
        when (p1?.getIntExtra("action", -1)) {
            Env.STOP_SERVICE -> {
                serviceControl.stopService()
            }
            else -> {}
        }
    }
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
object ConnectorServiceManager {
    private val v2RayPoint: V2RayPoint = Libv2ray.newV2RayPoint(V2RayCallback(), true)

    private var config = ""

    private val handler = Handler()

    var serviceControl: SoftReference<ServiceControl>? = null
        set(value) {
            field = value
            Seq.setContext(value?.get()?.getService()?.application)
            Libv2ray.initV2Env(
                value?.get()?.getService()?.getExternalFilesDir("assets")?.absolutePath,
                Utils.getAndroidID(value?.get()?.getService()?.contentResolver!!)
            )
        }

    fun startV2Ray(context: Context) {
        if (v2RayPoint.isRunning) {
            return
        }
        Toast.makeText(context, "启动中……", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url("${Env.KIRARA_API}/config").build()
                val response = Utils.http(context).newCall(request).execute()
                when (response.code) {
                    200 -> {
                        config = response.body?.string() ?: ""
                        val intent = Intent(context, ConnectorVpnService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        }
                        else {
                            context.startService(intent)
                        }
                    }
                    403 -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "因为账号有些问题，部分功能暂时受限", Toast.LENGTH_LONG).show()
                        }
                    }
                    404 -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "你还没有使用资格哟！", Toast.LENGTH_LONG).show()
                        }
                    }
                    410 -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "账号过期了！", Toast.LENGTH_LONG).show()
                        }
                    }
                    in 500..599 -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "服务器炸了！", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                if (response.code != 200) {
                    val intent = Intent()
                    intent.action = Env.UI_CHANNEL
                    intent.putExtra("action", Env.START_FAILED)
                    context.sendBroadcast(intent)
                }
                response.close()
            }
            catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    Toast.makeText(context, "网络好像不太好~", Toast.LENGTH_LONG).show()
                    val intent = Intent()
                    intent.action = Env.UI_CHANNEL
                    intent.putExtra("action", Env.START_FAILED)
                    context.sendBroadcast(intent)
                }
            }
        }
    }

    fun startV2RayPoint() {
        val service = serviceControl?.get()?.getService() ?: return
        if (v2RayPoint.isRunning) {
            return
        }
        if (config.isEmpty()) {
            return
        }
        val filter = IntentFilter(Env.SERVICE_CHANNEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            service.registerReceiver(handler, filter, Context.RECEIVER_EXPORTED)
        }
        else {
            service.registerReceiver(handler, filter)
        }
        v2RayPoint.configureFileContent = config
        v2RayPoint.domainName = Env.PROXY_HOST
        try {
            v2RayPoint.runLoop(false)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        val intent = Intent()
        intent.action = Env.UI_CHANNEL
        if (v2RayPoint.isRunning) {
            intent.putExtra("action", Env.SERVICE_STARTED)
        }
        else {
            intent.putExtra("action", Env.SERVICE_STOPPED)
        }
        service.sendBroadcast(intent)
    }

    fun stopV2rayPoint() {
        val service = serviceControl?.get()?.getService() ?: return
        if (v2RayPoint.isRunning) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    v2RayPoint.stopLoop()
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        val intent = Intent()
        intent.action = Env.UI_CHANNEL
        intent.putExtra("action", Env.SERVICE_STOPPED)
        service.sendBroadcast(intent)
        service.unregisterReceiver(handler)
    }
}