package com.example.securevpn


import androidx.lifecycle.ViewModel
import com.example.securevpn.vpn.VpnState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    private val _vpnState =
        MutableStateFlow(
            VpnState.DISCONNECTED
        )

    val vpnState: StateFlow<VpnState> =
        _vpnState.asStateFlow()

    fun setState(
        state: VpnState
    ) {

        _vpnState.value = state
    }
}