package com.example.securevpn.vpn

import android.content.Context
import android.content.Intent
import android.os.Build

class VpnServiceManager private constructor(context: Context) {

    private val appContext: Context = context.applicationContext

    fun connect(server: VpnConfig): Result<Unit> {
        return try {
            val intent = Intent(appContext, SecureVpnService::class.java).apply {
                action = SecureVpnService.ACTION_CONNECT
                putExtra(SecureVpnService.EXTRA_SERVER_NAME, server.name)
                putExtra(SecureVpnService.EXTRA_SERVER_COUNTRY, server.country)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            VpnStateStore.setState(VpnState.ERROR)
            Result.failure(e)
        }
    }

    fun disconnect(): Result<Unit> {
        return try {
            val intent = Intent(appContext, SecureVpnService::class.java).apply {
                action = SecureVpnService.ACTION_DISCONNECT
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            VpnStateStore.setState(VpnState.ERROR)
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: VpnServiceManager? = null

        fun getInstance(context: Context): VpnServiceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VpnServiceManager(context).also { INSTANCE = it }
            }
        }
    }
}
