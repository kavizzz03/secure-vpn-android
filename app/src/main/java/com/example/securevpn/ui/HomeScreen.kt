package com.example.securevpn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.securevpn.vpn.VpnState

@Composable
fun HomeScreen(
    state: VpnState,
    killSwitchEnabled: Boolean,
    onKillSwitchChanged: (Boolean) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {

    val connected =
        state == VpnState.CONNECTED

    val connecting =
        state == VpnState.CONNECTING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Text(
            text = "SecureVPN",
            style =
                MaterialTheme.typography
                    .headlineLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Private & Secure Connection",
            style =
                MaterialTheme.typography
                    .bodyMedium
        )

        Spacer(
            modifier = Modifier.height(50.dp)
        )

        Column(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme
                        .surfaceVariant
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text = if (connected) {
                    "ON"
                } else {
                    "OFF"
                },
                style =
                    MaterialTheme.typography
                        .headlineMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = when {

                    connecting ->
                        "Connecting..."

                    connected ->
                        "Protected"

                    state == VpnState.ERROR ->
                        "Error"

                    else ->
                        "Not Connected"
                }
            )
        }

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Button(
            onClick = {

                if (connected) {
                    onDisconnect()
                } else {
                    onConnect()
                }
            },
            enabled = !connecting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape =
                RoundedCornerShape(16.dp)
        ) {

            Text(
                text = when {

                    connecting ->
                        "CONNECTING..."

                    connected ->
                        "DISCONNECT"

                    else ->
                        "CONNECT"
                }
            )
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors =
                CardDefaults.cardColors()
        ) {

            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {

                Text(
                    text = "VPN Server",
                    style =
                        MaterialTheme.typography
                            .titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "🇸🇬  Singapore"
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                TextButton(
                    onClick = {
                        // Server selection
                        // will be added later
                    }
                ) {

                    Text(
                        "Change Server"
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {

                    Text(
                        text = "Kill Switch",
                        style =
                            MaterialTheme.typography
                                .titleMedium
                    )

                    Text(
                        text =
                            "Block traffic if VPN disconnects",
                        style =
                            MaterialTheme.typography
                                .bodySmall
                    )
                }

                Switch(
                    checked =
                        killSwitchEnabled,
                    onCheckedChange =
                        onKillSwitchChanged
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "SecureVPN v1.0",
            style =
                MaterialTheme.typography
                    .bodySmall
        )
    }
}