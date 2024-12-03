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
import com.king250.kirafan.ui.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.SoftReference

class ConnectorVpnService : VpnService(), ServiceControl {
    private val isSpecial = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    private val app = if (isSpecial) {"com.vmos.pro"} else {"com.aniplex.kirarafantasia"}

    private var isRunning = false

    private lateinit var fd: ParcelFileDescriptor

    init {
        System.loadLibrary("hev-socks5-tunnel")
    }

    @Suppress("FunctionName")
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
        ConnectorServiceManager.serviceControl = SoftReference(this)
    }

    override fun onRevoke() {
        stopV2Ray()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ConnectorServiceManager.startV2RayPoint()
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, Env.NOTIFICATION_CHANNEL)
            .setContentTitle("GNet™ VPN Gateway Connector")
            .setContentText("已连接至SparkleFantasia CN组织机构专用网络")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
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
        val builder = Builder()
        builder.setMtu(1500)
        builder.addAddress("26.26.26.1", 30)
        builder.addRoute("0.0.0.0", 0)
        builder.addDnsServer("1.1.1.1")
        builder.setSession(getString(R.string.app_name))
        builder.addAllowedApplication(app)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ConnectorServiceManager.stopV2rayPoint()
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
            CoroutineScope(Dispatchers.IO).launch {
                if (isRunning) {
                    val dir = getExternalFilesDir("assets")?.absolutePath
                    TProxyStartService(File(dir, "tunnel.yaml").absolutePath, fd.fd)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopService()
        }
    }
}