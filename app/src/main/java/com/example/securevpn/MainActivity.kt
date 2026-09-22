package com.example.securevpn

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.securevpn.ui.HomeScreen
import com.example.securevpn.vpn.SecureVpnService
import com.example.securevpn.vpn.VpnState

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var killSwitchEnabled by mutableStateOf(false)

    /*
     * VPN permission result
     */
    private val vpnPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {
                startVpnService()
            } else {
                viewModel.setState(VpnState.ERROR)
            }
        }

    /*
     * Notification permission result
     */
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                connectVpn()
            } else {
                /*
                 * Notification permission is useful for the
                 * VPN foreground service.
                 *
                 * We still allow the VPN flow to continue,
                 * but Android may limit the notification.
                 */
                connectVpn()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val state by viewModel.vpnState.collectAsState()

            MaterialTheme {

                Surface {

                    HomeScreen(
                        state = state,

                        killSwitchEnabled =
                            killSwitchEnabled,

                        onKillSwitchChanged = { enabled ->
                            killSwitchEnabled = enabled
                        },

                        onConnect = {
                            requestNotificationPermissionAndConnect()
                        },

                        onDisconnect = {
                            disconnectVpn()
                        }
                    )
                }
            }
        }
    }

    /**
     * Request notification permission before starting
     * the foreground VPN service.
     */
    private fun requestNotificationPermissionAndConnect() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permission =
                Manifest.permission.POST_NOTIFICATIONS

            val granted =
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {

                notificationPermissionLauncher.launch(
                    permission
                )

                return
            }
        }

        connectVpn()
    }

    /**
     * Start the Android VPN permission flow.
     */
    private fun connectVpn() {

        /*
         * Prevent multiple connection attempts.
         */
        if (
            viewModel.vpnState.value ==
            VpnState.CONNECTING ||
            viewModel.vpnState.value ==
            VpnState.CONNECTED
        ) {
            return
        }

        viewModel.setState(
            VpnState.CONNECTING
        )

        val vpnPermissionIntent =
            VpnService.prepare(this)

        if (vpnPermissionIntent != null) {

            /*
             * Android will display the VPN permission
             * confirmation screen.
             */
            vpnPermissionLauncher.launch(
                vpnPermissionIntent
            )

        } else {

            /*
             * VPN permission was already granted.
             */
            startVpnService()
        }
    }

    /**
     * Start SecureVpnService.
     */
    private fun startVpnService() {

        try {

            val intent =
                Intent(
                    this,
                    SecureVpnService::class.java
                ).apply {
                    action =
                        SecureVpnService.ACTION_CONNECT
                }

            ContextCompat.startForegroundService(
                this,
                intent
            )

            /*
             * IMPORTANT:
             *
             * Do NOT immediately set CONNECTED here.
             *
             * The service still needs to create the VPN
             * interface and eventually establish the real
             * WireGuard tunnel.
             *
             * For now we keep CONNECTING.
             *
             * Later SecureVpnService will report:
             *
             * CONNECTING -> CONNECTED
             *
             * only after the actual tunnel is ready.
             */

        } catch (exception: Exception) {

            viewModel.setState(
                VpnState.ERROR
            )
        }
    }

    /**
     * Stop the VPN service.
     */
    private fun disconnectVpn() {

        /*
         * Nothing to disconnect.
         */
        if (
            viewModel.vpnState.value ==
            VpnState.DISCONNECTED
        ) {
            return
        }

        viewModel.setState(
            VpnState.DISCONNECTING
        )

        try {

            val intent =
                Intent(
                    this,
                    SecureVpnService::class.java
                ).apply {
                    action =
                        SecureVpnService.ACTION_DISCONNECT
                }

            startService(intent)

            viewModel.setState(
                VpnState.DISCONNECTED
            )

        } catch (exception: Exception) {

            viewModel.setState(
                VpnState.ERROR
            )
        }
    }

    override fun onDestroy() {

        /*
         * We intentionally do not automatically
         * disconnect the VPN when the Activity closes.
         *
         * The VPN service can continue running in
         * the background.
         */

        super.onDestroy()
    }
}