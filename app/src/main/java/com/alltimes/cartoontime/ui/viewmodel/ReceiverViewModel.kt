package com.alltimes.cartoontime.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.UIState
import com.alltimes.cartoontime.data.network.BLEServerManager
import com.alltimes.cartoontime.data.network.UWBControllerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiverViewModel(application: Application) : AndroidViewModel(application) {

    
    // BLE 서버 매니저 할당
    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private val server: BLEServerManager = BLEServerManager(application)
    private val uwbCommunicator: UWBControllerManager = UWBControllerManager(application)

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
//            server.controllerReceived.collect { names ->
//                _uiState.update { it.copy(namesReceived = names) }
        }
    }

    // 버튼 이벤트 리스너 시작 스탑 토글
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    fun onButtonClick() {
        _uiState.update { it.copy(isRunning = !it.isRunning) }
        if (uiState.value.isRunning) {
            startServer()
        } else {
            stopServer()
        }
    }

    // 서버 시작
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    private fun startServer() {
        viewModelScope.launch {
            // BLE 서버 시작
            server.startServer()
            // UWB 연결 준비
        }
    }

    // 서버 정지
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    private fun stopServer() {
        viewModelScope.launch {
            // BLE 서버 정지
            server.stopServer()
            // UWB 연결 해제
        }
    }
}