package com.alltimes.cartoontime.ui.viewmodel

import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.UIStateModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiverViewModel(application: Application) : AndroidViewModel(application) {

    private val bleViewModel: BLEViewModel = BLEViewModel(application)
    private val uwbViewModel: UWBViewModel = UWBViewModel(application)

    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    fun onButtonClick() {
        _uiState.update { it.copy(isRunning = !it.isRunning) }
        bleViewModel.onButtonClick()
    }

    fun connectToDevice(address: String) {
        uwbViewModel.connectToDevice(address)
    }
}
