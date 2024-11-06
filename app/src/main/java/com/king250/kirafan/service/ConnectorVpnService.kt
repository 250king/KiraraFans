package com.king250.kirafan.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.StrictMode
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

    private lateinit var process: Process

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
        mainIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, Env.NOTIFICATION_CHANNEL)
            .setContentTitle("GNet™ VPN Gateway")
            .setContentText("已连接至主节点")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
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
            process.destroy()
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
        val socksPort = 10808
        val cmd = arrayListOf(
            File(applicationContext.applicationInfo.nativeLibraryDir, "libtun2socks.so").absolutePath,
            "--netif-ipaddr", "26.26.26.2",
            "--netif-netmask", "255.255.255.252",
            "--socks-server-addr", "127.0.0.1:$socksPort",
            "--tunmtu", "1500",
            "--sock-path", "sock_path",
            "--enable-udprelay",
            "--loglevel", "notice",
        )
        try {
            val proBuilder = ProcessBuilder(cmd)
            proBuilder.redirectErrorStream(true)
            process = proBuilder.directory(applicationContext.filesDir).start()
            Thread {
                process.waitFor()
                if (isRunning) {
                    runTun2socks()
                }
            }.start()
            sendFd()
        } catch (e: Exception) {
            e.printStackTrace()
            stopService()
        }
    }

    private fun sendFd() {
        val fd = fd.fileDescriptor
        val path = File(applicationContext.filesDir, "sock_path").absolutePath
        CoroutineScope(Dispatchers.IO).launch {
            var tries = 0
            while (true) {
                try {
                    Thread.sleep(50L shl tries)
                    LocalSocket().use { localSocket ->
                        localSocket.connect(LocalSocketAddress(path, LocalSocketAddress.Namespace.FILESYSTEM))
                        localSocket.setFileDescriptorsForSend(arrayOf(fd))
                        localSocket.outputStream.write(42)
                    }
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (tries > 5) break
                    tries += 1
                }
            }
        }
    }
}