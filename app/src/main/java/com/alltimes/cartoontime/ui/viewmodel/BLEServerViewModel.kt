package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.UIStateModel
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.ble.BLEServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BLEServerViewModel(private val context: Context) : ViewModel(), RangingCallback {

    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    // mode: true - login
    // mode: false - money transaction
    private val _mode = MutableStateFlow(false)
    val mode = _mode.asStateFlow()

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
        }
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    private fun stopServer() {
        viewModelScope.launch {
        }
    }


    ////////////////// uwb controller //////////////////

    private var measurementCount = 0
    private var sessionActive = false
    private val timeoutHandler = Handler(Looper.getMainLooper())

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    fun setSession(value: Boolean) {
        sessionActive = value
    }

    override fun onDistanceMeasured(distance: Float) {

        println("거리 측정 : ${distance}")

        if (!sessionActive) return

        println("active true 거리 측정 : ${distance}")

        // 거리 측정 로직 처리
        if (distance < 5) {
            measurementCount++
        } else {
            measurementCount = 0  // 거리 벗어나면 카운트 초기화
        }

        if (measurementCount >= 30) {
            sessionActive = false  // 세션 종료
            completeLogin()
        }

        // 10cm 이상 거리에서 타임아웃 처리
        timeoutHandler.postDelayed({
            if (distance > 10) {
                sessionActive = false  // 타임아웃 발생 시 세션 비활성화
                // 추가 처리
            }
        }, 3000)
    }

    private fun completeLogin() {
        // UI 상태 업데이트
        viewModelScope.launch {
            _uiState.update { it.copy(isLogin = true) }
        }
        // 화면 전환 처리
        // 예: navigateToMainScreen()
        println("입실 완료 처리")
    }
}
