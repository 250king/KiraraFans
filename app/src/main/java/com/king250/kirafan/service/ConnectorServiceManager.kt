package com.king250.kirafan.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.content.ClipboardManager
import com.king250.kirafan.Env
import com.king250.kirafan.ui.activity.MainActivity
import com.king250.kirafan.util.ClientUtil
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
            Env.STOP_SERVICE, Env.START_FAILED -> {
                serviceControl.stopService()
            }
            Env.COPY_URL -> {
                val cm = p0?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText(null, "https://vpc.sparklefantasia.com/get"))
                ClientUtil.toast(p0, "已成功复制到剪切板")
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
                ClientUtil.getAndroidId(value?.get()?.getService()?.contentResolver!!)
            )
        }

    fun startV2Ray(a: MainActivity) {
        if (v2RayPoint.isRunning) {
            return
        }
        a.v.showSnackBar("启动中……")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url("${Env.KIRARA_API}/config").build()
                val response = ClientUtil.http(a, true).newCall(request).execute()
                withContext(Dispatchers.Main) {
                    when (response.code) {
                        200 -> {
                            config = response.body?.string() ?: ""
                            val intent = Intent(a, ConnectorVpnService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                a.startForegroundService(intent)
                            }
                            else {
                                a.startService(intent)
                            }
                        }
                        401 -> {
                            a.logout()
                        }
                        403 -> {
                            a.v.showSnackBar("账号违规被封了（")
                        }
                        404 -> {
                            a.v.showSnackBar("没有相应的配置文件（")
                        }
                        410 -> {
                            a.v.showSnackBar("账号过期了（")
                        }
                        else -> {
                            a.v.showSnackBar("服务器炸了！")
                        }
                    }
                    if (response.code != 200) {
                        val intent = Intent()
                        intent.action = Env.UI_CHANNEL
                        intent.putExtra("action", Env.START_FAILED)
                        a.sendBroadcast(intent)
                    }
                    response.close()
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    a.v.showSnackBar("网络好像不太好哦~")
                    val intent = Intent()
                    intent.action = Env.UI_CHANNEL
                    intent.putExtra("action", Env.START_FAILED)
                    a.sendBroadcast(intent)
                }
            }
        }
    }

    fun startV2RayPoint() {
        val service = serviceControl?.get()?.getService() ?: return
        if (v2RayPoint.isRunning || config.isEmpty()) {
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