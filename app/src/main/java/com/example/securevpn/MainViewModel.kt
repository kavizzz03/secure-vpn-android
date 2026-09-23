package com.example.securevpn

import androidx.lifecycle.ViewModel
import com.example.securevpn.vpn.VpnState
import com.example.securevpn.vpn.VpnStateStore
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    val vpnState: StateFlow<VpnState> =
        VpnStateStore.state
}