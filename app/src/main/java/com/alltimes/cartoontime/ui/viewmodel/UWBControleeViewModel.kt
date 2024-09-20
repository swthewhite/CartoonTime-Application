package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.UIStateModel
import com.alltimes.cartoontime.data.model.UwbAddressModel // UwbAddressModel을 가져옵니다.
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.uwb.UwbControleeCommunicator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UWBControleeViewModel(private val context: Context) : ViewModel(), RangingCallback {

    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    private var measurementCount = 0
    private var sessionActive = false
    private val timeoutHandler = Handler(Looper.getMainLooper())

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    override fun onDistanceMeasured(distance: Float) {
        //if (!sessionActive) return

        println("거리 측정 : ${distance}")

        // 거리 측정 로직 처리
        if (distance < 5) {
            measurementCount++
        } else {
            measurementCount = 0  // 거리 벗어나면 카운트 초기화
        }

        if (measurementCount >= 30) {
            completeLogin()
            sessionActive = false  // 세션 종료
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
