package com.example.securevpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.securevpn.MainActivity
import com.example.securevpn.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class SecureVpnService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.example.securevpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.securevpn.DISCONNECT"
        const val EXTRA_SERVER_NAME = "extra_server_name"
        const val EXTRA_SERVER_COUNTRY = "extra_server_country"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "secure_vpn_channel"
    }

    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    @Volatile
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_CONNECT

        if (action == ACTION_DISCONNECT) {
            stopVpn()
            return START_NOT_STICKY
        }

        val serverName = intent?.getStringExtra(EXTRA_SERVER_NAME) ?: "Singapore"
        val serverCountry = intent?.getStringExtra(EXTRA_SERVER_COUNTRY) ?: "Singapore"

        startForeground(NOTIFICATION_ID, createNotification(serverName, serverCountry))
        startVpn(serverName)

        return START_STICKY
    }

    private fun startVpn(serverName: String) {
        VpnStateStore.setState(VpnState.CONNECTING)

        serviceJob?.cancel()
        serviceJob = scope.launch {
            try {
                parcelFileDescriptor?.close()

                val builder = Builder()
                builder.addAddress("10.2.0.2", 24)
                builder.addDnsServer("1.1.1.1")
                builder.addDnsServer("8.8.8.8")
                builder.addRoute("1.1.1.1", 32)
                builder.addRoute("8.8.8.8", 32)
                builder.setMtu(1400)
                builder.setSession("SecureVPN ($serverName)")

                val pendingIntent = PendingIntent.getActivity(
                    this@SecureVpnService,
                    0,
                    Intent(this@SecureVpnService, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                builder.setConfigureIntent(pendingIntent)

                parcelFileDescriptor = builder.establish()

                if (parcelFileDescriptor != null) {
                    isRunning = true
                    VpnStateStore.setState(VpnState.CONNECTED)
                    runDnsTunnelWorker(parcelFileDescriptor!!)
                } else {
                    VpnStateStore.setState(VpnState.ERROR)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                VpnStateStore.setState(VpnState.ERROR)
            }
        }
    }

    private fun runDnsTunnelWorker(pfd: ParcelFileDescriptor) {
        val inputStream = FileInputStream(pfd.fileDescriptor)
        val packetBuffer = ByteArray(32767)

        var dnsSocket: DatagramSocket? = null
        try {
            dnsSocket = DatagramSocket()
            protect(dnsSocket)
            dnsSocket.soTimeout = 2000

            val dnsServerIp = InetAddress.getByName("1.1.1.1")

            while (isRunning && VpnStateStore.state.value == VpnState.CONNECTED) {
                try {
                    val length = inputStream.read(packetBuffer)
                    if (length > 0) {
                        if (length > 28 && packetBuffer[9].toInt() == 17) {
                            val destPort = ((packetBuffer[22].toInt() and 0xFF) shl 8) or (packetBuffer[23].toInt() and 0xFF)
                            if (destPort == 53) {
                                val ipHeaderLen = (packetBuffer[0].toInt() and 0x0F) * 4
                                val udpHeaderLen = 8
                                val payloadOffset = ipHeaderLen + udpHeaderLen
                                val payloadLen = length - payloadOffset

                                if (payloadLen > 0) {
                                    val dnsPayload = ByteArray(payloadLen)
                                    System.arraycopy(packetBuffer, payloadOffset, dnsPayload, 0, payloadLen)

                                    val sendPacket = DatagramPacket(dnsPayload, payloadLen, dnsServerIp, 53)
                                    dnsSocket.send(sendPacket)

                                    val recvBuffer = ByteArray(4096)
                                    val recvPacket = DatagramPacket(recvBuffer, recvBuffer.size)
                                    try {
                                        dnsSocket.receive(recvPacket)
                                    } catch (e: Exception) {
                                        // Timeout
                                    }
                                }
                            }
                        }
                    } else {
                        Thread.sleep(50)
                    }
                } catch (e: Exception) {
                    if (!isRunning) break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                dnsSocket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopVpn() {
        isRunning = false
        VpnStateStore.setState(VpnState.DISCONNECTING)
        try {
            serviceJob?.cancel()
            parcelFileDescriptor?.close()
            parcelFileDescriptor = null
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            VpnStateStore.setState(VpnState.DISCONNECTED)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun createNotification(serverName: String, serverCountry: String): Notification {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SecureVPN Active Connection",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val disconnectIntent = Intent(this, SecureVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SecureVPN Active")
            .setContentText("Connected to $serverName ($serverCountry)")
            .setSmallIcon(R.drawable.ic_vpn)
            .setOngoing(true)
            .addAction(0, "Disconnect", disconnectPendingIntent)
            .build()
    }
}
