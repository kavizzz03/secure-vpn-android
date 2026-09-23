package com.example.securevpn.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VpnStateStore {

    private val _state =
        MutableStateFlow(VpnState.DISCONNECTED)

    val state: StateFlow<VpnState> =
        _state.asStateFlow()

    fun setState(state: VpnState) {
        _state.value = state
    }
}