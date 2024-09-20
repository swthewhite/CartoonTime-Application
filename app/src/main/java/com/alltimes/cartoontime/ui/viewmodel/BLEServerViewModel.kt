package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.UIStateModel
import com.alltimes.cartoontime.data.network.ble.BLEServerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BLEServerViewModel(private val context: Context, private val viewModel: UWBControllerViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    private val server: BLEServerManager = BLEServerManager(context, viewModel)

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    fun onButtonClick() {
        _uiState.update { it.copy(isRunning = !it.isRunning) }
        if (uiState.value.isRunning) {
            startServer()
        } else {
            stopServer()
        }
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    private fun startServer() {
        viewModelScope.launch {
            server.startServer()
        }
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    private fun stopServer() {
        viewModelScope.launch {
            server.stopServer()
        }
    }
}
