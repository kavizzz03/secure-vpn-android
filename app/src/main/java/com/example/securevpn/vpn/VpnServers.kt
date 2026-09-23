package com.example.securevpn.vpn

import android.content.Context
import com.example.securevpn.security.SecureStorage
import org.json.JSONArray
import org.json.JSONObject

object VpnServers {

    val defaultServers = listOf(
        VpnConfig(
            id = "singapore",
            name = "Singapore",
            country = "Singapore",
            flag = "🇸🇬",
            privateKey = "SGHby6xHozq+9BV4fwoznm0O/kpiNdux2hJYh5fVPUw=",
            address = "10.8.0.2/32",
            serverPublicKey = "trVxQln6bgTIUO8IwAEtkndTb8XQ0ie8igaNyqHwwVo=",
            endpoint = "203.0.113.10:51820"
        ),
        VpnConfig(
            id = "japan",
            name = "Japan",
            country = "Japan",
            flag = "🇯🇵",
            privateKey = "",
            address = "10.8.1.2/32",
            serverPublicKey = "trVxQln6bgTIUO8IwAEtkndTb8XQ0ie8igaNyqHwwVo=",
            endpoint = ""
        ),
        VpnConfig(
            id = "germany",
            name = "Germany",
            country = "Germany",
            flag = "🇩🇪",
            privateKey = "",
            address = "10.8.2.2/32",
            serverPublicKey = "trVxQln6bgTIUO8IwAEtkndTb8XQ0ie8igaNyqHwwVo=",
            endpoint = ""
        ),
        VpnConfig(
            id = "netherlands",
            name = "Netherlands",
            country = "Netherlands",
            flag = "🇳🇱",
            privateKey = "",
            address = "10.8.3.2/32",
            serverPublicKey = "trVxQln6bgTIUO8IwAEtkndTb8XQ0ie8igaNyqHwwVo=",
            endpoint = ""
        ),
        VpnConfig(
            id = "usa",
            name = "United States",
            country = "United States",
            flag = "🇺🇸",
            privateKey = "",
            address = "10.8.4.2/32",
            serverPublicKey = "trVxQln6bgTIUO8IwAEtkndTb8XQ0ie8igaNyqHwwVo=",
            endpoint = ""
        )
    )

    var servers: List<VpnConfig> = defaultServers
        private set

    fun loadCustomServers(context: Context) {
        val secureStorage = SecureStorage(context)
        val jsonString = secureStorage.get("custom_vpn_servers") ?: return
        try {
            val jsonArray = JSONArray(jsonString)
            val customList = mutableListOf<VpnConfig>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                customList.add(
                    VpnConfig(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        country = obj.optString("country", "Custom"),
                        flag = obj.optString("flag", "🌐"),
                        privateKey = obj.optString("privateKey", ""),
                        address = obj.optString("address", "10.8.0.2/32"),
                        serverPublicKey = obj.optString("serverPublicKey", ""),
                        endpoint = obj.optString("endpoint", ""),
                        allowedIps = obj.optString("allowedIps", "0.0.0.0/0, ::/0").split(",").map { it.trim() }.filter { it.isNotBlank() },
                        dnsServers = obj.optString("dnsServers", "1.1.1.1, 1.0.0.1").split(",").map { it.trim() }.filter { it.isNotBlank() },
                        mtu = obj.optInt("mtu", 1280),
                        free = true
                    )
                )
            }
            if (customList.isNotEmpty()) {
                servers = customList + defaultServers.filter { def -> customList.none { it.id == def.id } }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveServer(context: Context, config: VpnConfig) {
        val currentList = servers.toMutableList()
        val index = currentList.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            currentList[index] = config
        } else {
            currentList.add(0, config)
        }
        servers = currentList

        val secureStorage = SecureStorage(context)
        val jsonArray = JSONArray()
        for (server in servers) {
            val obj = JSONObject().apply {
                put("id", server.id)
                put("name", server.name)
                put("country", server.country)
                put("flag", server.flag)
                put("privateKey", server.privateKey)
                put("address", server.address)
                put("serverPublicKey", server.serverPublicKey)
                put("endpoint", server.endpoint)
                put("allowedIps", server.allowedIps.joinToString(","))
                put("dnsServers", server.dnsServers.joinToString(","))
                put("mtu", server.mtu)
            }
            jsonArray.put(obj)
        }
        secureStorage.save("custom_vpn_servers", jsonArray.toString())
    }

    fun parseWgQuickConfig(wgConfig: String, id: String, name: String): VpnConfig? {
        var privateKey = ""
        var address = "10.8.0.2/32"
        var dnsServers = listOf("1.1.1.1", "1.0.0.1")
        var mtu = 1280
        var publicKey = ""
        var endpoint = ""
        var allowedIps = listOf("0.0.0.0/0", "::/0")

        var currentSection = ""
        for (line in wgConfig.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || trimmed.isBlank()) continue
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                currentSection = trimmed.substring(1, trimmed.length - 1).lowercase()
                continue
            }
            val parts = trimmed.split("=", limit = 2)
            if (parts.size != 2) continue
            val key = parts[0].trim().lowercase()
            val value = parts[1].trim()

            when (currentSection) {
                "interface" -> {
                    when (key) {
                        "privatekey" -> privateKey = value
                        "address" -> address = value
                        "dns" -> dnsServers = value.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        "mtu" -> mtu = value.toIntOrNull() ?: 1280
                    }
                }
                "peer" -> {
                    when (key) {
                        "publickey" -> publicKey = value
                        "endpoint" -> endpoint = value
                        "allowedips" -> allowedIps = value.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    }
                }
            }
        }

        if (endpoint.isBlank() || publicKey.isBlank()) {
            return null
        }

        return VpnConfig(
            id = id,
            name = name,
            country = "Custom",
            flag = "⚡",
            privateKey = privateKey,
            address = address,
            serverPublicKey = publicKey,
            endpoint = endpoint,
            allowedIps = allowedIps,
            dnsServers = dnsServers,
            mtu = mtu,
            free = true
        )
    }
}
