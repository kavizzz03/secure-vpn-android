package com.example.securevpn.vpn

import android.content.Context
import com.example.securevpn.security.SecureStorage
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class WireGuardManager private constructor(context: Context) {

    private val appContext: Context = context.applicationContext
    private val backend: Backend by lazy { GoBackend(appContext) }
    private val secureStorage: SecureStorage = SecureStorage(appContext)
    private val tunnel: WireGuardTunnel = WireGuardTunnel("SecureVPN")

    suspend fun connect(config: VpnConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            VpnStateStore.setState(VpnState.CONNECTING)

            if (config.endpoint.isBlank() || config.serverPublicKey.isBlank()) {
                throw IllegalArgumentException("Server endpoint and public key are required. Please configure server details.")
            }

            val clientPrivateKey = getOrGeneratePrivateKey(config.id, config.privateKey)
            val wgConfigText = buildWgConfigString(config, clientPrivateKey)

            val parsedConfig = Config.parse(ByteArrayInputStream(wgConfigText.toByteArray(Charsets.UTF_8)))

            backend.setState(tunnel, Tunnel.State.UP, parsedConfig)
            VpnStateStore.setState(VpnState.CONNECTED)
            Result.success(Unit)
        } catch (e: Exception) {
            VpnStateStore.setState(VpnState.ERROR)
            Result.failure(e)
        }
    }

    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            VpnStateStore.setState(VpnState.DISCONNECTING)
            backend.setState(tunnel, Tunnel.State.DOWN, null)
            VpnStateStore.setState(VpnState.DISCONNECTED)
            Result.success(Unit)
        } catch (e: Exception) {
            VpnStateStore.setState(VpnState.ERROR)
            Result.failure(e)
        }
    }

    suspend fun connectCustomConfig(rawConfig: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            VpnStateStore.setState(VpnState.CONNECTING)
            val parsedConfig = Config.parse(ByteArrayInputStream(rawConfig.toByteArray(Charsets.UTF_8)))
            backend.setState(tunnel, Tunnel.State.UP, parsedConfig)
            VpnStateStore.setState(VpnState.CONNECTED)
            Result.success(Unit)
        } catch (e: Exception) {
            VpnStateStore.setState(VpnState.ERROR)
            Result.failure(e)
        }
    }

    fun getOrGeneratePrivateKey(serverId: String, fallbackPrivateKey: String): String {
        val keyStorageKey = "wg_private_key_$serverId"
        val existingKey = secureStorage.get(keyStorageKey)
        if (!existingKey.isNullOrBlank()) {
            return existingKey
        }
        if (fallbackPrivateKey.isNotBlank()) {
            secureStorage.save(keyStorageKey, fallbackPrivateKey)
            return fallbackPrivateKey
        }
        val newKeyPair = KeyPair()
        val newPrivateKey = newKeyPair.privateKey.toBase64()
        secureStorage.save(keyStorageKey, newPrivateKey)
        return newPrivateKey
    }

    fun getOrGeneratePublicKey(serverId: String, fallbackPrivateKey: String = ""): String {
        val privateKeyBase64 = getOrGeneratePrivateKey(serverId, fallbackPrivateKey)
        return try {
            val privateKey = Key.fromBase64(privateKeyBase64)
            val keyPair = KeyPair(privateKey)
            keyPair.publicKey.toBase64()
        } catch (e: Exception) {
            ""
        }
    }

    private fun buildWgConfigString(config: VpnConfig, clientPrivateKey: String): String {
        val sb = StringBuilder()
        sb.append("[Interface]\n")
        sb.append("PrivateKey = ").append(clientPrivateKey).append("\n")
        if (config.address.isNotBlank()) {
            sb.append("Address = ").append(config.address).append("\n")
        }
        if (config.dnsServers.isNotEmpty()) {
            sb.append("DNS = ").append(config.dnsServers.joinToString(", ")).append("\n")
        }
        if (config.mtu > 0) {
            sb.append("MTU = ").append(config.mtu).append("\n")
        }
        sb.append("\n[Peer]\n")
        if (config.serverPublicKey.isNotBlank()) {
            sb.append("PublicKey = ").append(config.serverPublicKey).append("\n")
        }
        if (config.endpoint.isNotBlank()) {
            sb.append("Endpoint = ").append(config.endpoint).append("\n")
        }
        if (config.allowedIps.isNotEmpty()) {
            sb.append("AllowedIPs = ").append(config.allowedIps.joinToString(", ")).append("\n")
        } else {
            sb.append("AllowedIPs = 0.0.0.0/0, ::/0\n")
        }
        return sb.toString()
    }

    class WireGuardTunnel(private val tunnelName: String) : Tunnel {
        override fun getName(): String = tunnelName
        override fun onStateChange(newState: Tunnel.State) {
            when (newState) {
                Tunnel.State.UP -> VpnStateStore.setState(VpnState.CONNECTED)
                Tunnel.State.DOWN -> VpnStateStore.setState(VpnState.DISCONNECTED)
                Tunnel.State.TOGGLE -> {}
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: WireGuardManager? = null

        fun getInstance(context: Context): WireGuardManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WireGuardManager(context).also { INSTANCE = it }
            }
        }
    }
}
