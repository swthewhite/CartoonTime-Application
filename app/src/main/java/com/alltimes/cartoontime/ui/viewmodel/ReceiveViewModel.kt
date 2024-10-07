package com.alltimes.cartoontime.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.common.MessageListener
import com.alltimes.cartoontime.data.model.fcm.FcmMessage
import com.alltimes.cartoontime.data.model.ui.UIStateModel
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.ble.BLEServerManager
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.repository.FCMRepository
import com.alltimes.cartoontime.data.repository.UserInfoUpdater
import com.alltimes.cartoontime.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiveViewModel(application: Application, private val context: Context) : BaseViewModel(application), MessageListener,
    RangingCallback {

    /////////////////////////// 공용 ///////////////////////////

    private val _balance = MutableStateFlow(sharedPreferences.getLong("balance", 0L))
    val balance: StateFlow<Long> = _balance

    private val userInfoUpdater: UserInfoUpdater = UserInfoUpdater(
        UserRepository(RetrofitClient.apiService),
        sharedPreferences
    )

    private var timeoutHandler: Handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null


    val fcmRepository = FCMRepository(this)

    var isFCMActive = false

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    val ReceiverId = sharedPreferences.getLong("userId", -1L)

    private val server: BLEServerManager =
        BLEServerManager(application, ReceiverId.toString(), "WITCH", this)

    private var measurementCount = 0
    private var sessionActive = false

    init {
        val fcmToken = sharedPreferences.getString("fcmToken", "") ?: ""
        println("receiveViewModel FCM Token: $fcmToken")
        fcmRepository.listenForMessages(fcmToken)
        isFCMActive = true

        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
        }

        startTimeout()
    }

    fun onPuaseAll() {
        isFCMActive = false
    }

    fun onResumeAll() {
        UpdateUserInfo()
        isFCMActive = true
    }

    private fun startTimeout() {
        // 이전 타이머 제거
        timeoutRunnable?.let { timeoutHandler.removeCallbacks(it) }

        // 새 타이머 설정 (1분)
        timeoutRunnable = Runnable {
            // 타임아웃 발생 시 처리할 코드
            Toast.makeText(context, "타임 아웃 ... !! 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }

        timeoutHandler.postDelayed(timeoutRunnable!!, 60_000) // 60초 후 실행
    }

    // 화면 전환이 발생할 때 호출
    fun onScreenChanged() {
        startTimeout() // 타이머 리셋
    }

    override fun onMessageReceived(message: FcmMessage) {
        if (!isFCMActive) return
        println("RECEIVE 메시지 수신 완료: $message")
        if (message.content.contains("포인트")) {
            // 특정 동작 수행
            _content.value = message.content

            UpdateUserInfo()

            // 메인 스레드에서 goScreen 호출
            viewModelScope.launch {
                goScreen(ScreenType.RECEIVECONFIRM)
            }
        }
    }

    fun UpdateUserInfo() {
        userInfoUpdater.updateUserInfo(sharedPreferences.getLong("userId", -1L))
    }

    /////////////////////////// BLE 서버 및 UWB 기능 ///////////////////////////

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    fun transactionBleServerStart() {
        println("서버 시작 ~~~~~")
        // 서버 시작
        onButtonClick()
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    fun onButtonClick() {
        _uiState.update { it.copy(isRunning = !_uiState.value.isRunning) }
        if (_uiState.value.isRunning) {
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

    fun setSession(value: Boolean) {
        sessionActive = value
    }

    override fun onDistanceMeasured(distance: Float) {

    }
}
