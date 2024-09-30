package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.common.MessageListener
import com.alltimes.cartoontime.data.model.FcmMessage
import com.alltimes.cartoontime.data.model.UIStateModel
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

class ReceiveViewModel(private val context: Context) : ViewModel(), MessageListener, RangingCallback {

    /////////////////////////// 공용 ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    val editor = sharedPreferences.edit()

    private val _balance = MutableStateFlow(sharedPreferences.getLong("balance", 0L))
    val balance: StateFlow<Long> = _balance

    private val userInfoUpdater: UserInfoUpdater = UserInfoUpdater(
        UserRepository(RetrofitClient.apiService),
        sharedPreferences
    )

    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }

    val fcmRepository = FCMRepository(this)

    var isFCMActive = false

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    // mode: true - login
    // mode: false - money transaction
    private val _mode = MutableStateFlow(false)
    val mode = _mode.asStateFlow()

    private val server: BLEServerManager = BLEServerManager(context, this)

    private var measurementCount = 0
    private var sessionActive = false
    private val timeoutHandler = Handler(Looper.getMainLooper())

    init {
        val fcmToken = sharedPreferences.getString("fcmToken", "") ?: ""
        println("receiveViewModel FCM Token: $fcmToken")
        fcmRepository.listenForMessages(fcmToken)
        isFCMActive = true

        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    fun onPuaseAll() {
        isFCMActive = false
    }

    fun onResumeAll() {
        UpdateUserInfo()
        isFCMActive = true
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
        // 모드 설정
        setMode(true)
        // 서버 시작
        onButtonClick()
    }

    fun setMode(value: Boolean) {
        _mode.update { value }
        server.setMode(value)
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
        println("거리 측정 : $distance")

        if (!sessionActive) return

        println("세션 활성화 상태에서 거리 측정 : $distance")

        // 거리 측정 로직 처리
        if (distance < 5) {
            measurementCount++
        } else {
            measurementCount = 0  // 거리 벗어나면 카운트 초기화
        }

        if (measurementCount >= 30) {
            server.disconnectUWB()
            sessionActive = false  // 세션 종료
            completeLogin()
        }

        // 10cm 이상 거리에서 타임아웃 처리
        timeoutHandler.postDelayed({
            if (distance > 10) {
                server.disconnectUWB()
                sessionActive = false  // 타임아웃 발생 시 세션 비활성화
                // 추가 처리 로직
            }
        }, 3000)
    }

    private fun completeLogin() {
        // UI 상태 업데이트
        viewModelScope.launch {
            _uiState.update { it.copy(isLogin = true) }
            // 화면 전환 처리
            goScreen(ScreenType.RECEIVECONFIRM)
        }
        println("입실 완료 처리")
    }
}
