package com.example.securevpn.vpn

data class VpnConfig(
    val id: String,
    val name: String,
    val country: String,
    val flag: String,

    /*
     * Client private key.
     *
     * IMPORTANT:
     * Never commit a real private key to GitHub.
     */
    val privateKey: String = "",

    /*
     * VPN client address.
     *
     * Example:
     * 10.8.0.2/32
     */
    val address: String = "10.8.0.2/32",

    /*
     * WireGuard server public key.
     */
    val serverPublicKey: String = "",

    /*
     * Server endpoint.
     *
     * Example:
     * 203.0.113.10:51820
     */
    val endpoint: String = "",

    /*
     * Full tunnel.
     */
    val allowedIps: List<String> =
        listOf(
            "0.0.0.0/0",
            "::/0"
        ),

    /*
     * DNS servers.
     */
    val dnsServers: List<String> =
        listOf(
            "1.1.1.1",
            "1.0.0.1"
        ),

    val mtu: Int = 1280,

    val free: Boolean = true
) {
    fun isConfigured(): Boolean = endpoint.isNotBlank() && serverPublicKey.isNotBlank()
}
