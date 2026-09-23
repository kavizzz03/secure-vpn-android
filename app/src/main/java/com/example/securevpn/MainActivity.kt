package com.example.securevpn

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.securevpn.security.SecureStorage
import com.example.securevpn.ui.HomeScreen
import com.example.securevpn.ui.theme.AppThemeMode
import com.example.securevpn.ui.theme.SecureVPNTheme
import com.example.securevpn.vpn.VpnServers
import com.example.securevpn.vpn.VpnServiceManager
import com.example.securevpn.vpn.VpnState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var vpnServiceManager: VpnServiceManager
    private lateinit var secureStorage: SecureStorage

    private var killSwitchEnabled by mutableStateOf(false)
    private var selectedServerId by mutableStateOf("singapore")
    private var appThemeMode by mutableStateOf(AppThemeMode.SYSTEM)

    private val vpnPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                connectToSelectedServer()
            } else {
                Toast.makeText(this, "VPN permission was denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            requestVpnPermission()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        secureStorage = SecureStorage(applicationContext)

        // Restore theme mode preference
        val storedTheme = secureStorage.get("app_theme_mode")
        if (storedTheme != null) {
            try {
                appThemeMode = AppThemeMode.valueOf(storedTheme)
            } catch (e: Exception) {
                appThemeMode = AppThemeMode.SYSTEM
            }
        }

        VpnServers.loadCustomServers(applicationContext)

        if (VpnServers.servers.isNotEmpty()) {
            selectedServerId = VpnServers.servers.first().id
        }

        vpnServiceManager = VpnServiceManager.getInstance(applicationContext)

        setContent {
            val state by viewModel.vpnState.collectAsState()

            SecureVPNTheme(themeMode = appThemeMode) {
                HomeScreen(
                    state = state,
                    themeMode = appThemeMode,
                    onThemeModeChanged = { newTheme ->
                        appThemeMode = newTheme
                        secureStorage.save("app_theme_mode", newTheme.name)
                    },
                    killSwitchEnabled = killSwitchEnabled,
                    onKillSwitchChanged = { enabled ->
                        killSwitchEnabled = enabled
                        if (enabled) {
                            try {
                                Toast.makeText(
                                    this,
                                    "Opening System VPN Settings. Enable 'Block connections without VPN' for Kill Switch.",
                                    Toast.LENGTH_LONG
                                ).show()
                                startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
                            } catch (e: Exception) {
                                Toast.makeText(this, "Kill Switch active", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onServerSelected = { serverId ->
                        selectedServerId = serverId
                    },
                    selectedServerId = selectedServerId,
                    onConnect = {
                        requestPermissionsAndConnect()
                    },
                    onDisconnect = {
                        disconnectVpn()
                    },
                    onAddCustomConfig = { config ->
                        VpnServers.saveServer(applicationContext, config)
                        selectedServerId = config.id
                        Toast.makeText(this, "Saved custom server ${config.name}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun requestPermissionsAndConnect() {
        if (viewModel.vpnState.value == VpnState.CONNECTED || viewModel.vpnState.value == VpnState.CONNECTING) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(permission)
                return
            }
        }

        requestVpnPermission()
    }

    private fun requestVpnPermission() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            connectToSelectedServer()
        }
    }

    private fun connectToSelectedServer() {
        val server = VpnServers.servers.firstOrNull { it.id == selectedServerId }

        if (server == null) {
            Toast.makeText(this, "VPN server profile not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val result = vpnServiceManager.connect(server)
            result.onFailure { exception ->
                Toast.makeText(
                    this@MainActivity,
                    exception.message ?: "VPN connection failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun disconnectVpn() {
        if (viewModel.vpnState.value == VpnState.DISCONNECTED) {
            return
        }

        lifecycleScope.launch {
            val result = vpnServiceManager.disconnect()
            result.onFailure { exception ->
                Toast.makeText(
                    this@MainActivity,
                    exception.message ?: "VPN disconnect failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
