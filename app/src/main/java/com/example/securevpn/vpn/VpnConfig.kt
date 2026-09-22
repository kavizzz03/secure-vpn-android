package com.example.securevpn.vpn

data class VpnConfig(

    val sessionName: String = "SecureVPN",

    val vpnAddress: String = "10.8.0.2",

    val vpnPrefixLength: Int = 24,

    val mtu: Int = 1280,

    val dnsServers: List<String> = listOf(
        "1.1.1.1",
        "1.0.0.1"
    ),

    val ipv4Route: String = "0.0.0.0",

    val ipv4RoutePrefix: Int = 0
)