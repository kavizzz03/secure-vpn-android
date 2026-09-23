package com.example.securevpn.vpn

import android.content.Context
import com.example.securevpn.security.SecureStorage
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object FreeVpnRepository {

    private const val WARP_REG_URL = "https://api.cloudflareclient.com/v0a2158/reg"
    private const val WARP_SERVER_PUBLIC_KEY = "bmXOC+F1FxEMF9mTpd24F2I6ARAh3XOHAcBwA4zTNm0="

    suspend fun getOrProvisionServerConfig(
        context: Context,
        server: VpnConfig
    ): VpnConfig = withContext(Dispatchers.IO) {

        // If the server already has a valid endpoint and public key, return as is.
        if (server.endpoint.isNotBlank() && server.serverPublicKey.isNotBlank()) {
            return@withContext server
        }

        val secureStorage = SecureStorage(context)
        val storageKey = "free_config_${server.id}"

        val cachedPrivateKey = secureStorage.get("${storageKey}_private_key")
        val cachedAddress = secureStorage.get("${storageKey}_address")
        val cachedEndpoint = secureStorage.get("${storageKey}_endpoint")

        if (!cachedPrivateKey.isNullOrBlank() && !cachedAddress.isNullOrBlank() && !cachedEndpoint.isNullOrBlank()) {
            return@withContext server.copy(
                privateKey = cachedPrivateKey,
                address = cachedAddress,
                serverPublicKey = WARP_SERVER_PUBLIC_KEY,
                endpoint = cachedEndpoint,
                allowedIps = listOf("0.0.0.0/0", "::/0"),
                dnsServers = listOf("1.1.1.1", "1.0.0.1"),
                mtu = 1280
            )
        }

        try {
            val keyPair = KeyPair()
            val clientPrivateKey = keyPair.privateKey.toBase64()
            val clientPublicKey = keyPair.publicKey.toBase64()

            val url = URL(WARP_REG_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("User-Agent", "okhttp/3.12.1")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.doOutput = true

            val jsonBody = JSONObject().apply {
                put("install_id", "")
                put("fcm_token", "")
                put("tos", "2020-09-01T00:00:00.000Z")
                put("key", clientPublicKey)
                put("type", "Android")
                put("locale", "en_US")
            }

            conn.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
            }

            if (conn.responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)
                val configObj = responseJson.getJSONObject("result").getJSONObject("config")

                val interfaceObj = configObj.getJSONObject("interface")
                val addressesArr = interfaceObj.getJSONObject("addresses")
                val ipv4Address = addressesArr.getString("v4")

                val peersArr = configObj.getJSONArray("peers")
                val peerObj = peersArr.getJSONObject(0)
                val endpointObj = peerObj.getJSONObject("endpoint")
                val endpointV4 = endpointObj.getString("v4")
                val serverPubKey = peerObj.optString("public_key", WARP_SERVER_PUBLIC_KEY)

                secureStorage.save("${storageKey}_private_key", clientPrivateKey)
                secureStorage.save("${storageKey}_address", "$ipv4Address/32")
                secureStorage.save("${storageKey}_endpoint", endpointV4)

                return@withContext server.copy(
                    privateKey = clientPrivateKey,
                    address = "$ipv4Address/32",
                    serverPublicKey = serverPubKey,
                    endpoint = endpointV4,
                    allowedIps = listOf("0.0.0.0/0", "::/0"),
                    dnsServers = listOf("1.1.1.1", "1.0.0.1"),
                    mtu = 1280
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback default WARP endpoint if online registration fails
        val fallbackKeyPair = KeyPair()
        val fallbackPrivateKey = fallbackKeyPair.privateKey.toBase64()
        val fallbackEndpoint = getEndpointForLocation(server.id)

        server.copy(
            privateKey = fallbackPrivateKey,
            address = "172.16.0.2/32",
            serverPublicKey = WARP_SERVER_PUBLIC_KEY,
            endpoint = fallbackEndpoint,
            allowedIps = listOf("0.0.0.0/0", "::/0"),
            dnsServers = listOf("1.1.1.1", "1.0.0.1"),
            mtu = 1280
        )
    }

    private fun getEndpointForLocation(locationId: String): String {
        return when (locationId) {
            "singapore" -> "162.159.192.1:2408"
            "japan" -> "162.159.193.1:2408"
            "germany" -> "162.159.195.1:2408"
            "netherlands" -> "188.114.96.1:2408"
            "usa" -> "188.114.97.1:2408"
            else -> "162.159.192.1:2408"
        }
    }
}
