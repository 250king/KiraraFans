package com.king250.kirafan.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.king250.kirafan.Env
import com.king250.kirafan.R
import com.king250.kirafan.activity.MainActivity
import com.king250.kirafan.handler.ConnectorHandler
import com.king250.kirafan.handler.ServiceHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.SoftReference

class ConnectorService : VpnService(), ServiceHandler {
    private var isRunning = false

    private lateinit var fd: ParcelFileDescriptor

    private lateinit var tunnel: Job

    init {
        System.loadLibrary("hev-socks5-tunnel")
    }

    @Suppress("FunctionName", "unused")
    private external fun TProxyStartService(configPath: String, fd: Int)

    @Suppress("FunctionName")
    private external fun TProxyStopService()

    @Keep
    @Suppress("FunctionName", "unused")
    private external fun TProxyGetStats(): LongArray

    override fun onCreate() {
        super.onCreate()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        ConnectorHandler.control = SoftReference(this)
    }

    override fun onRevoke() {
        stopV2Ray()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ConnectorHandler.startV2RayPoint()
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        val backIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, Env.NOTIFICATION_CHANNEL).apply {
            setContentTitle("GNet™ VPN Gateway Connector")
            setContentText("已连接至SparkleFantasia CN组织机构专用网络")
            setSmallIcon(R.drawable.ic_notification)
            setContentIntent(backIntent)
            setAutoCancel(false)
            setOngoing(true)
        }.build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun getService(): Service {
        return this
    }

    override fun startService() {
        val prepare = prepare(this)
        if (prepare != null) {
            return
        }
        val builder = Builder().apply {
            setMtu(1500)
            addAddress("26.26.26.1", 30)
            addRoute("0.0.0.0", 0)
            addDnsServer("223.5.5.5")
            setSession(getString(R.string.app_name))
            addAllowedApplication(Env.TARGET_PACKAGE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setMetered(false)
            }
        }
        try {
            fd.close()
        }
        catch (_: Exception) {}
        try {
            fd = builder.establish()!!
            isRunning = true
            runTun2socks()
        }
        catch (e: Exception) {
            e.printStackTrace()
            stopV2Ray()
        }
    }

    override fun stopService() {
        stopV2Ray()
    }

    override fun vpnProtect(socket: Int): Boolean {
        return protect(socket)
    }

    private fun stopV2Ray(isForced: Boolean = true) {
        isRunning = false
        try {
            TProxyStopService()
            tunnel.cancel()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        ConnectorHandler.stopV2rayPoint()
        if (isForced) {
            stopSelf()
            try {
                fd.close()
            }
            catch (_: Exception) {}
        }
    }

    private fun runTun2socks() {
        try {
            tunnel = CoroutineScope(Dispatchers.IO).launch {
                if (isRunning) {
                    val dir = getExternalFilesDir("assets")?.absolutePath
                    TProxyStartService(File(dir, "tunnel.yaml").absolutePath, fd.fd)
                }
            }
            tunnel.start()
        }
        catch (e: Exception) {
            e.printStackTrace()
            stopService()
        }
    }
}