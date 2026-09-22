package com.example.securevpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.securevpn.R

class SecureVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    private var running = false

    companion object {

        const val ACTION_CONNECT =
            "com.kavizz.securevpn.CONNECT"

        const val ACTION_DISCONNECT =
            "com.kavizz.securevpn.DISCONNECT"

        const val CHANNEL_ID =
            "secure_vpn_channel"

        const val NOTIFICATION_ID =
            1001
    }

    override fun onCreate() {

        super.onCreate()

        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_CONNECT -> {

                startForegroundNotification()

                connect()
            }

            ACTION_DISCONNECT -> {

                disconnect()
            }
        }

        return START_STICKY
    }

    private fun startForegroundNotification() {

        val notification =
            createNotification(
                "SecureVPN is connecting..."
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo
                    .FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )

        } else {

            startForeground(
                NOTIFICATION_ID,
                notification
            )
        }
    }

    private fun createNotification(
        text: String
    ): Notification {

        return NotificationCompat
            .Builder(
                this,
                CHANNEL_ID
            )
            .setContentTitle(
                "SecureVPN"
            )
            .setContentText(
                text
            )
            .setSmallIcon(
                R.drawable.ic_vpn
            )
            .setOngoing(true)
            .setCategory(
                NotificationCompat.CATEGORY_SERVICE
            )
            .setPriority(
                NotificationCompat.PRIORITY_LOW
            )
            .build()
    }

    private fun connect() {

        if (running) {
            return
        }

        try {

            val config =
                VpnConfig()

            vpnInterface =
                Builder()
                    .setSession(
                        config.sessionName
                    )
                    .setMtu(
                        config.mtu
                    )
                    .addAddress(
                        config.vpnAddress,
                        config.vpnPrefixLength
                    )
                    .addRoute(
                        config.ipv4Route,
                        config.ipv4RoutePrefix
                    )
                    .addDnsServer(
                        config.dnsServers[0]
                    )
                    .addDnsServer(
                        config.dnsServers[1]
                    )
                    .establish()

            if (vpnInterface == null) {

                updateNotification(
                    "VPN interface could not be created"
                )

                stopSelf()

                return
            }

            running = true

            updateNotification(
                "VPN interface connected"
            )

            /*
             * IMPORTANT:
             *
             * At this point Android has created the TUN
             * interface.
             *
             * The remote encrypted tunnel has NOT been
             * implemented yet.
             *
             * WireGuard integration will be added here.
             */

        } catch (exception: Exception) {

            running = false

            vpnInterface?.close()

            vpnInterface = null

            updateNotification(
                "VPN error: ${exception.message}"
            )
        }
    }

    private fun disconnect() {

        running = false

        vpnInterface?.close()

        vpnInterface = null

        stopForeground(STOP_FOREGROUND_REMOVE)

        stopSelf()
    }

    private fun updateNotification(
        text: String
    ) {

        val notification =
            createNotification(text)

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            NOTIFICATION_ID,
            notification
        )
    }

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "VPN Connection",
                    NotificationManager.IMPORTANCE_LOW
                )

            channel.description =
                "SecureVPN connection status"

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }

    override fun onDestroy() {

        running = false

        vpnInterface?.close()

        vpnInterface = null

        super.onDestroy()
    }

    override fun onRevoke() {

        running = false

        vpnInterface?.close()

        vpnInterface = null

        stopSelf()

        super.onRevoke()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return super.onBind(intent)
    }
}