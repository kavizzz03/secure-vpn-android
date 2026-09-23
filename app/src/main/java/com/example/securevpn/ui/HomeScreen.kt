package com.example.securevpn.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.securevpn.R
import com.example.securevpn.ui.theme.AppThemeMode
import com.example.securevpn.ui.theme.StatusConnected
import com.example.securevpn.ui.theme.StatusConnecting
import com.example.securevpn.ui.theme.StatusDisconnected
import com.example.securevpn.ui.theme.StatusError
import com.example.securevpn.vpn.VpnConfig
import com.example.securevpn.vpn.VpnServers
import com.example.securevpn.vpn.VpnState
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun HomeScreen(
    state: VpnState,
    themeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    killSwitchEnabled: Boolean,
    onKillSwitchChanged: (Boolean) -> Unit,
    selectedServerId: String,
    onServerSelected: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onAddCustomConfig: (VpnConfig) -> Unit = {}
) {
    val connected = state == VpnState.CONNECTED
    val connecting = state == VpnState.CONNECTING
    val changing = state == VpnState.DISCONNECTING

    val selectedServer = VpnServers.servers.firstOrNull {
        it.id == selectedServerId
    }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showEditServerDialog by remember { mutableStateOf<VpnConfig?>(null) }

    // Timer state for connected duration
    var secondsConnected by remember { mutableStateOf(0L) }
    LaunchedEffect(connected) {
        if (connected) {
            secondsConnected = 0L
            while (true) {
                delay(1000L)
                secondsConnected++
            }
        } else {
            secondsConnected = 0L
        }
    }

    // Animation for pulse effect
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(connecting) {
        if (connecting) {
            pulseScale.animateTo(
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.snapTo(1f)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            /*
             * Top Header Bar with Logo and Theme Selector
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // App Logo from res/drawable/logo.jpg
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .padding(2.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "SecureVPN Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SecureVPN",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Next-Gen Encrypted Tunnel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Theme Mode Selector Button
                IconButton(
                    onClick = { showThemeDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = themeMode.icon,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            /*
             * Main Central Power Connection Button
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Glow Ring
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(pulseScale.value)
                        .clip(CircleShape)
                        .background(
                            when {
                                connected -> StatusConnected.copy(alpha = 0.15f)
                                connecting -> StatusConnecting.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            }
                        )
                )

                // Outer Button Ring
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .border(
                            width = 4.dp,
                            brush = Brush.sweepGradient(
                                colors = when {
                                    connected -> listOf(StatusConnected, Color(0xFF00B0FF), StatusConnected)
                                    connecting -> listOf(StatusConnecting, Color(0xFFFF6D00), StatusConnecting)
                                    else -> listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                    )
                                }
                            ),
                            shape = CircleShape
                        )
                        .clickable(
                            enabled = !connecting && !changing,
                            onClick = {
                                if (connected) onDisconnect() else onConnect()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when {
                                connected -> "⚡"
                                connecting -> "🔄"
                                state == VpnState.ERROR -> "⚠️"
                                else -> "🔒"
                            },
                            fontSize = 38.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when {
                                connecting -> "CONNECTING"
                                changing -> "STOPPING"
                                connected -> "STOP"
                                else -> "CONNECT"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = when {
                                connected -> StatusConnected
                                connecting -> StatusConnecting
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            /*
             * Status Indicator & Timer
             */
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when {
                        connecting -> "Establishing WireGuard Tunnel..."
                        changing -> "Disconnecting..."
                        connected -> "PROTECTED • ${formatDuration(secondsConnected)}"
                        state == VpnState.ERROR -> "Connection Failed"
                        else -> "NOT CONNECTED"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            connected -> StatusConnected
                            connecting -> StatusConnecting
                            state == VpnState.ERROR -> StatusError
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                )

                if (connected && selectedServer != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "IP Protected via WireGuard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            /*
             * Active VPN Server Card
             */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = selectedServer?.flag ?: "🌐",
                            fontSize = 32.sp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = selectedServer?.name ?: "Select Server",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (selectedServer?.endpoint.isNullOrBlank()) "Cloudflare High-Speed Network"
                                else "Endpoint: ${selectedServer?.endpoint}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ 24 ms",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            /*
             * Server List & Import Config
             */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Free Locations",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        OutlinedButton(
                            onClick = { showImportDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "+ .conf",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    LazyColumn {
                        items(
                            items = VpnServers.servers,
                            key = { it.id }
                        ) { server ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        enabled = !connected && !connecting,
                                        onClick = { onServerSelected(server.id) }
                                    )
                                    .background(
                                        if (server.id == selectedServerId) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else Color.Transparent
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = server.flag,
                                    fontSize = 24.sp
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = server.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = "${server.country} • Free High Speed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (server.id == selectedServerId) {
                                    Text(
                                        text = "✓",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /*
             * System Kill Switch Switch
             */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kill Switch Protection",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Block internet if VPN drops (System Lockdown)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = killSwitchEnabled,
                        onCheckedChange = onKillSwitchChanged
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    /*
     * Theme Mode Selector Dialog
     */
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "Select App Theme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    AppThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onThemeModeChanged(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (mode == themeMode),
                                onClick = {
                                    onThemeModeChanged(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${mode.icon} ${mode.title}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    /*
     * Import Custom WireGuard .conf Dialog
     */
    if (showImportDialog) {
        ImportConfigDialog(
            onDismiss = { showImportDialog = false },
            onImport = { newConfig ->
                onAddCustomConfig(newConfig)
                showImportDialog = false
            }
        )
    }

    /*
     * Edit Server Profile Dialog
     */
    if (showEditServerDialog != null) {
        val serverToEdit = showEditServerDialog!!
        EditServerDialog(
            server = serverToEdit,
            onDismiss = { showEditServerDialog = null },
            onSave = { updatedConfig ->
                onAddCustomConfig(updatedConfig)
                showEditServerDialog = null
            }
        )
    }
}

@Composable
fun ImportConfigDialog(
    onDismiss: () -> Unit,
    onImport: (VpnConfig) -> Unit
) {
    var profileName by remember { mutableStateOf("My WireGuard Server") }
    var configText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import WireGuard Config (.conf)") },
        text = {
            Column {
                Text(
                    text = "Paste your WireGuard wg-quick (.conf) configuration below:",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = configText,
                    onValueChange = {
                        configText = it
                        errorMessage = ""
                    },
                    label = { Text("[Interface] ... [Peer] ...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    maxLines = 10
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val id = "custom_" + System.currentTimeMillis()
                    val parsed = VpnServers.parseWgQuickConfig(configText, id, profileName.ifBlank { "Custom Server" })
                    if (parsed != null) {
                        onImport(parsed)
                    } else {
                        errorMessage = "Invalid WireGuard config. Endpoint and PublicKey in [Peer] are required."
                    }
                }
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditServerDialog(
    server: VpnConfig,
    onDismiss: () -> Unit,
    onSave: (VpnConfig) -> Unit
) {
    var name by remember { mutableStateOf(server.name) }
    var endpoint by remember { mutableStateOf(server.endpoint) }
    var serverPublicKey by remember { mutableStateOf(server.serverPublicKey) }
    var address by remember { mutableStateOf(server.address) }
    var privateKey by remember { mutableStateOf(server.privateKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Server (${server.name})") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Server Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("Server Endpoint (e.g., 203.0.113.1:51820)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = serverPublicKey,
                    onValueChange = { serverPublicKey = it },
                    label = { Text("Server Public Key") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Client Address (e.g., 10.8.0.2/32)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = privateKey,
                    onValueChange = { privateKey = it },
                    label = { Text("Client Private Key (optional, auto-generated if empty)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = server.copy(
                        name = name.ifBlank { server.name },
                        endpoint = endpoint.trim(),
                        serverPublicKey = serverPublicKey.trim(),
                        address = address.trim().ifBlank { "10.8.0.2/32" },
                        privateKey = privateKey.trim()
                    )
                    onSave(updated)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }
}
