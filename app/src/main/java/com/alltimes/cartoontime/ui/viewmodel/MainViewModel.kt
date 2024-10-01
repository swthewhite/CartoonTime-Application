package com.alltimes.cartoontime.ui.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.common.MessageListener
import com.alltimes.cartoontime.data.model.Cartoon
import com.alltimes.cartoontime.data.model.FcmMessage
import com.alltimes.cartoontime.data.model.SendUiState
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.ble.BLEClient
import com.alltimes.cartoontime.data.network.ble.BLEScanner
import com.alltimes.cartoontime.data.network.uwb.UWBControlee
import com.alltimes.cartoontime.data.remote.ComicResponse
import com.alltimes.cartoontime.data.remote.FCMRequest
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.repository.FCMRepository
import com.alltimes.cartoontime.data.repository.UserInfoUpdater
import com.alltimes.cartoontime.data.repository.UserRepository
import com.alltimes.cartoontime.utils.AccelerometerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class MainViewModel(private val context: Context) : ViewModel(), MessageListener {

    /////////////////////////// 공용 ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    val editor = sharedPreferences.edit()

    // 내 유저 이름
    val name = sharedPreferences.getString("name", "")

    // 내 fcmToken
    val fcmToken = sharedPreferences.getString("fcmToken", "")

    // 내 유저ID
    val userId = sharedPreferences.getLong("userId", 0L)

    // 키오스크 UWB 데이터
    var partnerUwbData: String? = null

    // 잔액 및 유저 상태 관리
    private val userInfoUpdater: UserInfoUpdater = UserInfoUpdater(
        UserRepository(RetrofitClient.apiService),
        sharedPreferences
    )

    // 요금
    private val _charge = MutableStateFlow(-1L)
    val charge: StateFlow<Long> = _charge

    // 잔액
    private val _balance = MutableStateFlow(sharedPreferences.getLong("balance", 1000000L))
    val balance: StateFlow<Long> = _balance

    // 입퇴실 상태
    private val _state = MutableStateFlow(sharedPreferences.getString("state", "입실 전"))
    val state: MutableStateFlow<String?> = _state

    // 입실 시간
    private val _enteredTime =
        MutableStateFlow(sharedPreferences.getString("enteredTime", "2024-08-19 09:00:00"))
    val enteredTime: MutableStateFlow<String?> = _enteredTime

    // 액티비티 전환
    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    // 스크린 전환
    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }

    // 서버 통신 관련 변수
    private val repository = UserRepository(RetrofitClient.apiService)

    // 파이어베이스 통신 관련 변수
    private val fcmRepository = FCMRepository(this)

    // 특정 화면일 때만 메시지 처리
    var isFCMActive = false

    init {
        fcmRepository.listenForMessages(fcmToken!!)
        isFCMActive = true

        // 서버 api 호출
        CoroutineScope(Dispatchers.IO).launch {
            val fcmRequest = FCMRequest(userId, fcmToken)

            val response = try {
                repository.saveFcmToken(fcmRequest)
            } catch (e: Exception) {
                null
            }

            if (response?.success == true) {
                println("fcmToken 저장 성공")
            } else {
                println("fcmToken 저장 실패")
            }
        }
    }

    // 유저 정보 업데이트
    fun UpdateUserInfo() {
        userInfoUpdater.updateUserInfo(sharedPreferences.getLong("userId", -1L))
    }

    // 화면 전환시 호출
    // 모든 기능 정지
    fun onPuaseAll() {
        accelerometerStop()

        isFCMActive = false
    }

    // 화면이 돌아왔을 때 호출
    // 모든 기능 시작
    fun onResumeAll() {
        UpdateUserInfo()

        isFCMActive = true
    }

    /////////////////////////// Main ///////////////////////////

    // 각속도 측정용
    private lateinit var accelerometerManager: AccelerometerManager
    private var accelerometerCount by Delegates.notNull<Int>()
    private var accelerometerIsCounting = true // 1분 동안 카운팅 방지용 플래그

    // FCM 메시지 수신
    override fun onMessageReceived(message: FcmMessage) {
        if (!isFCMActive) return

        if (message.content.contains("입퇴실")) {
            // 입퇴실 완료
            onKioskLoadingCompleted()
        } else if (message.content.contains("원")) {
            // 퇴실 과정 중 실패
            // 메시지에서 charge 값을 추출
            val chargePattern = Regex("(\\d+)원")
            val matchResult = chargePattern.find(message.content)

            // charge 값을 할당
            _charge.value = matchResult!!.groupValues[1].toLong()
            _state.value = "입실 완료"
            goScreen(ScreenType.CONFIRM)
        }
    }

    fun accelerometerStart(lifecycleOwner: LifecycleOwner) {
        println("각속도 측정 시작")
        accelerometerManager = AccelerometerManager(context)
        accelerometerCount = 0
        accelerometerManager.start()

        accelerometerManager.accelerometerData.observe(lifecycleOwner) { data ->
            // 데이터 업데이트
            if (data.z <= -9.0 && accelerometerIsCounting) {
                // 아래를 보는 중
                accelerometerCount++
                if (accelerometerCount >= 10) {
                    onLoginOut()
                    accelerometerCount = 0
                    disableCounting() // 카운팅 방지
                }
            } else if (data.z >= 0) {
                // 위를 보는 중
                accelerometerCount = 0
            }

        }
    }

    fun accelerometerStop() {
        println("각속도 측정 중지")
        accelerometerManager.stop()
    }

    private fun disableCounting() {
        accelerometerIsCounting = false // 카운팅 방지
        CoroutineScope(Dispatchers.Main).launch {
            delay(10000) // 대기
            accelerometerIsCounting = true // 다시 카운팅 활성화
        }
    }

    // 현재 시각을 가져오는 함수
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    /////////////////////////// *UWB* ///////////////////////////
    private val uwbCommunicator = UWBControlee(context)

    /////////////////////////// *BLEScanner* ///////////////////////////
    private val bleScanner = BLEScanner(context)

    private val triedDevices = mutableSetOf<String>()

    private val _activeConnection = MutableStateFlow<BLEClient?>(null)
    val activeConnection: MutableStateFlow<BLEClient?> get() = _activeConnection

    // 4. BLE 스캔
    private val _uiState = MutableStateFlow(SendUiState())
    val uiState = _uiState.asStateFlow()

    private var kioskLoadingJob: Job? = null

    // 각속도 측정 후 입퇴실 진행
    fun onLoginOut() {
        if (_state.value == "입실 전") _state.value = "입실 중"
        else _state.value = "퇴실 중"

        kioskLoginOut()

        kioskLoadingJob?.cancel()  // 이전 작업이 있으면 취소
        kioskLoadingJob = CoroutineScope(Dispatchers.Main).launch {
            delay(20000)
            onKioskLoadingTimeout()  // 타임아웃 시 실행할 함수 호출
        }
    }

    @SuppressLint("MissingPermission")
    fun onKioskLoadingTimeout() {
        // 타임아웃 처리 로직 추가
        _uiState.update { it.copy(isScanning = false) }
        bleScanner.stop()
        if (_state.value == "입실 중") _state.value = "입실 전"
        else _state.value = "입실 완료"
    }

    /**
     * BLE 스캔 시작
     */
    @SuppressLint("MissingPermission")
    fun kioskLoginOut() {
        _uiState.value = _uiState.value.copy(isSending = !_uiState.value.isSending)

        if (_uiState.value.isSending) {
            startScanningAndConnect()
        } else {
            disconnectDevice()
        }
    }

    /**
     * BLEClient를 이용하여 BLEDeviceConnection을 초기화
     */
    @SuppressLint("MissingPermission")
    fun startScanningAndConnect() {
        viewModelScope.launch {
            Log.d("MainViewModel", "Scanning started")
            _uiState.update { it.copy(isScanning = true) }

            CoroutineScope(Dispatchers.Main).launch {
                bleScanner.start("KIOSK")

                bleScanner.foundDevices.collectLatest { devices ->
                    for (device in devices) {
                        if (!triedDevices.contains(device.address)) {
                            communicateBLEClient(device)
                        }
                    }
                }
            }
        }
    }

    private var measurementCount = 0

    @SuppressLint("MissingPermission")
    private suspend fun communicateBLEClient(device: BluetoothDevice) {
        bleScanner.addConnectedDevice(device.address)

        _activeConnection.value =
            BLEClient(context, device, uwbCommunicator.getUWBAddress(), userId.toString(), "KIOSK")
        val activeConnection: BLEClient? = _activeConnection.value

        withContext(Dispatchers.Main) {
            activeConnection?.connect()

            // 기기가 연결될 때까지 대기
            activeConnection?.isConnected?.collectLatest { isConnected ->
                if (isConnected) {
                    Log.d("BLEConnection", "기기 연결 성공: ${device.name}")


                    // 서비스 검색 완료 대기
                    CoroutineScope(Dispatchers.Main).launch {
                        activeConnection?.discoverServices()
                        activeConnection?.serviceDiscoveryCompleted?.collectLatest { servicesDiscovered ->
                            if (servicesDiscovered) {
                                Log.d("BLEConnection", "서비스 검색 완료.")

                                // 특성 작업을 순차적으로 실행
                                val characteristicSuccess =
                                    activeConnection?.communicate()

                                if (characteristicSuccess == true) {
                                    Log.d("BLEConnection", "특성 읽기 및 쓰기 작업 완료.")

                                    // StateFlow 데이터를 구독하여 최신 값 할당
                                    viewModelScope.launch {
                                        activeConnection.partnerUWBData.collectLatest { data ->
                                            partnerUwbData = data
                                            // UWB 데이터를 처리
                                            val splitUwbData =
                                                partnerUwbData?.split("/") ?: listOf("", "")
                                            val address = splitUwbData.getOrNull(0) ?: ""
                                            val channel = splitUwbData.getOrNull(1) ?: ""

                                            val callback = object : RangingCallback {
                                                override fun onDistanceMeasured(distance: Float) {
                                                    println("거리 측정 : ${distance}")
                                                }
                                            }
                                            // uwb Ranging 시작
                                            measurementCount = 0
                                            uwbCommunicator.createRanging(
                                                address,
                                                channel,
                                                callback
                                            )
                                        }
                                    }

                                    _uiState.update {
                                        it.copy(
                                            isDeviceConnected = true,
                                            activeDevice = device
                                        )
                                    }
                                } else {
                                    Log.e("BLEConnection", "특성 작업 중 실패.")
                                }
                            } else {
                                Log.e("BLEConnection", "서비스 검색 실패.")
                            }
                        }
                    }
                } else {
                    Log.e("BLEConnection", "기기 연결 실패: ${device.name}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice() {
        Log.d("MainViewModel", "Disconnecting device")
        _activeConnection.value?.disconnect()
        _uiState.update { it.copy(isDeviceConnected = false, activeDevice = null) }

        triedDevices.clear()


        // Ensure scanning is stopped if needed
        if (_uiState.value.isScanning) {
            bleScanner.stop()
            Log.d("MainViewModel", "Scanning stopped after disconnecting")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCleared() {
        super.onCleared()

        if (bleScanner.isScanning.value) {
            Log.d("MainViewModel", "Stopping scanning on ViewModel cleared")
            bleScanner.stop()
        }
    }

    // 입실 중 -> 입실 완료
    // 퇴실 중 -> 입실 전
    fun onKioskLoadingCompleted() {

        println("onLoginOut")
        println("state: ${_state.value}")
        println("get state : ${sharedPreferences.getString("state", "입실 전")}")

        if (_state.value == "입실 중") {
            _state.value = "입실 완료"
            editor.putString("state", "입실 완료")
            editor.apply()

            // 현재 시각을 기록
            val currentTime = getCurrentTime()  // 현재 시각을 가져오는 함수
            _enteredTime.value = currentTime
            editor.putString("enteredTime", currentTime)
            editor.apply()
        } else if (_state.value == "퇴실 중") {
            _state.value = "입실 전"
            editor.putString("state", "입실 전")
            editor.apply()
            goScreen(ScreenType.CONFIRM)
        }
    }

    /////////////////////////// BookRecommend ///////////////////////////

    private val _cartoons = MutableStateFlow<List<ComicResponse>>(emptyList())
    val cartoons: StateFlow<List<ComicResponse>> = _cartoons

    private val _clickedCartoon = MutableStateFlow<ComicResponse?>(null)
    val clickedCartoon: StateFlow<ComicResponse?> = _clickedCartoon

    private val _category = MutableStateFlow("사용자 취향 만화")
    val category: StateFlow<String> get() = _category

    fun bookRecommendInit() {
        fetchCartoons(_category.value)
    }

    fun onClickedCategory(category: String) {
        _category.value = category
        fetchCartoons(category)
    }

    fun onClickedCartoon(cartoon: ComicResponse) {
        if (_clickedCartoon.value == cartoon) {
            _clickedCartoon.value = null
        } else {
            _clickedCartoon.value = cartoon
        }
    }

    fun fetchCartoons(category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allComics = repository.getAllComics()
                // 필요에 따라 카테고리별로 필터링
                _cartoons.value = allComics
            } catch (e: Exception) {
                _cartoons.value = emptyList()
            }
        }
    }


    /////////////////////////// Confirm ///////////////////////////

    fun handleChargeConfirm() {
        if (_balance.value < _charge.value) goActivity(ActivityType.CHARGE)
        else {
            UpdateUserInfo()
            goScreen(ScreenType.MAIN)
        }
    }
}