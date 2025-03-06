package com.king250.kirafan.handler

import libv2ray.V2RayVPNServiceSupportsSet

class V2RayHandler : V2RayVPNServiceSupportsSet {
    override fun shutdown(): Long {
        val serviceControl = ConnectorHandler.control?.get() ?: return -1
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
        val serviceControl = ConnectorHandler.control?.get() ?: return true
        return serviceControl.vpnProtect(p0.toInt())
    }

    override fun onEmitStatus(p0: Long, p1: String?): Long {
        return 0
    }

    override fun setup(p0: String?): Long {
        val serviceControl = ConnectorHandler.control?.get() ?: return -1
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