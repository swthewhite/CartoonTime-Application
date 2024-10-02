package com.alltimes.cartoontime.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.common.NumpadAction
import com.alltimes.cartoontime.common.PointpadAction
import com.alltimes.cartoontime.data.model.SendUiState
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.ble.BLEClient
import com.alltimes.cartoontime.data.network.ble.BLEScanner
import com.alltimes.cartoontime.data.network.uwb.UWBControlee
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.remote.TransferRequest
import com.alltimes.cartoontime.data.repository.FCMRepository
import com.alltimes.cartoontime.data.repository.UserRepository
import com.alltimes.cartoontime.ui.handler.NumPadClickHandler
import com.alltimes.cartoontime.ui.handler.PointPadClickHandler
import com.alltimes.cartoontime.utils.AccelerometerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class SendViewModel(application: Application, private val context: Context) : BaseViewModel(application), NumpadAction, PointpadAction {
    private val _activeConnection = MutableStateFlow<BLEClient?>(null)
    val activeConnection: MutableStateFlow<BLEClient?> get() = _activeConnection


    /////////////////////////// *공용* ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    val editor = sharedPreferences.edit()

    val SenderId = sharedPreferences.getLong("userId", -1L)

    var partnerUwbData: String? = null
    var ReceiverId: String? = null

    var inputEnable: Boolean = true

    private val _balance = MutableStateFlow(sharedPreferences.getLong("balance", 0L))
    val balance: StateFlow<Long> = _balance

    private val _partnerUserName = MutableStateFlow("")
    val partnerUserName: StateFlow<String> = _partnerUserName

    private val repository = UserRepository(RetrofitClient.apiService)

    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }


    /////////////////////////// *UWB* ///////////////////////////
    private val uwbCommunicator = UWBControlee(context)


    /////////////////////////// *BLEScanner* ///////////////////////////
    private val bleScanner = BLEScanner(context)

    /////////////////////////// 2. PointInput ///////////////////////////

    // 각속도 측정
    private lateinit var accelerometerManager: AccelerometerManager
    private var accelerometerCount by Delegates.notNull<Int>()

    fun accelerometerStart(lifecycleOwner: LifecycleOwner) {
        println("각속도 측정 시작")
        accelerometerManager = AccelerometerManager(context)
        accelerometerCount = 0
        accelerometerManager.start()

        accelerometerManager.accelerometerData.observe(lifecycleOwner) { data ->
            // 데이터 업데이트
            println("x: ${data.x}, y: ${data.y}, z: ${data.z}")
            if (data.z <= -9.0) {
                // 아래를 보는 중
                accelerometerCount++
                if (accelerometerCount >= 10) {
                    _isAcceptOpen.value = true
                    accelerometerCount = 0
                    accelerometerStop()
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

    /////////////////////////// PointInput ///////////////////////////

    override fun onPointClickedButton(type: Int) {
        pointPadClickHandler.onClickedButton(type, balance.value)
    }

    val point: StateFlow<String> get() = pointPadClickHandler.point

    // 눌리는 버튼에 대한 구현
    private val pointPadClickHandler: PointPadClickHandler by lazy {
        PointPadClickHandler(
            context = context,
            isPointExceeded = {
                if (point.value.toIntOrNull() ?: 0 > balance.value) {
                    showPointError()
                    pointPadClickHandler.setPoint(balance.value.toString())
                }
            }
        )
    }

    private fun showPointError() {
        Toast.makeText(context, "포인트가 초과되었습니다", Toast.LENGTH_SHORT).show()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }

    // 휴대폰 진동
    private fun triggerVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }


    /////////////////////////// 3. PasswordInput ///////////////////////////

    override fun onClickedButton(type: Int) {
        numPadClickHandler.onClickedButton(type)
    }

    val password: StateFlow<String> get() = numPadClickHandler.password

    private val numPadClickHandler: NumPadClickHandler by lazy {
        NumPadClickHandler(
            context = context,
            onPasswordComplete = { password: String ->
                val userPassword = sharedPreferences.getString("password", null)
                if (userPassword == password) {
                    findingPartner()
                    goScreen(ScreenType.SENDPARTNERCHECK)
                } else {
                    numPadClickHandler.clearPassword()
                    showPasswordError()
                }
            }
        )
    }

    /**
     * 비밀번호가 다를 때 에러 메시지 출력
     */
    private fun showPasswordError() {
        Toast.makeText(context, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }

    /////////////////////////// 4. PartnerCheck ///////////////////////////

    private val triedDevices = mutableSetOf<String>()

    // 4. BLE 스캔
    private val _uiState = MutableStateFlow(SendUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * BLE 스캔 시작
     */
    @SuppressLint("MissingPermission")
    fun findingPartner() {
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
            Log.d("SendViewModel", "Scanning started")
            _uiState.update { it.copy(isScanning = true) }

            CoroutineScope(Dispatchers.Main).launch {
                bleScanner.start("WITCH")

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

    @SuppressLint("MissingPermission")
    private suspend fun communicateBLEClient(device: BluetoothDevice) {
        bleScanner.addConnectedDevice(device.address)

        _activeConnection.value =
            BLEClient(
                context,
                device,
                uwbCommunicator.getUWBAddress(),
                SenderId.toString(),
                "WITCH"
            )
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

                                    viewModelScope.launch {
                                        activeConnection.partnerIdData.collectLatest { id ->
                                            ReceiverId = id

                                            // 상대정보 받아오기
                                            val partnerUserResponse =
                                                repository.getUserInfo(ReceiverId!!.toLong())

                                            if (partnerUserResponse.success) {
                                                _partnerUserName.value =
                                                    partnerUserResponse.data?.name!!
                                            }
                                            // userID 데이터를 처리
                                            Log.d("BLEConnection", "UserID: $ReceiverId")
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

    /**
     * Partner 수락
     */
    fun startTransaction(bleClient: BLEClient) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("SendViewModel", "Partner $isAcceptOpen")
            goScreen(ScreenType.SENDDESCRIPTION)
            readyUWB(bleClient)
        }
    }

    /**
     * Partner 거절
     */
    fun setUiState(value: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeviceConnected = value) }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice() {
        Log.d("SendViewModel", "Disconnecting device")
        _activeConnection.value?.disconnect()
        _uiState.update { it.copy(isDeviceConnected = false, activeDevice = null) }

        triedDevices.clear()


        // Ensure scanning is stopped if needed
        if (_uiState.value.isScanning) {
            bleScanner.stop()
            Log.d("SendViewModel", "Scanning stopped after disconnecting")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCleared() {
        super.onCleared()

        if (bleScanner.isScanning.value) {
            Log.d("SendViewModel", "Stopping scanning on ViewModel cleared")
            bleScanner.stop()
        }
    }


    /////////////////////////// 9. Description ///////////////////////////
    private val _isAcceptOpen = MutableStateFlow(false)
    val isAcceptOpen: StateFlow<Boolean> = _isAcceptOpen
    private var measurementCount = 0
    private val timeoutHandler = Handler(Looper.getMainLooper())

    // 각속도 센서 데이터 감지
    @SuppressLint("MissingPermission")
    suspend fun readyUWB(bleClient: BLEClient) {
        // 각속도 센서 데이터 감지
        Log.d("SendViewModel", "UWB 준비 중")
        isAcceptOpen.collectLatest { isOpen ->
            if (isOpen) {
                triggerVibration(context)
                _isAcceptOpen.value = false  // isAcceptOpen 상태 초기화

                Log.d("SendViewModel", "UWB 준비 완료")
                // 감지 시 uwbStart 특성 값 쓰기
                val uwbData = _activeConnection.value?.partnerUWBData
                val splitUwbData = uwbData?.value?.split("/") ?: listOf("", "")
                val address = splitUwbData.getOrNull(0) ?: ""
                val channel = splitUwbData.getOrNull(1) ?: ""

                val callback = object : RangingCallback {
                    override fun onDistanceMeasured(distance: Float) {
                        DistanceMeasured(distance)
                    }
                }
                // uwb Ranging 시작
                measurementCount = 0
                Log.d("SendViewModel", "Ranging 시작")
                CoroutineScope(Dispatchers.Main).launch {
                    val characteristicSuccess =
                        bleClient?.startUwbRanging()
                    if (characteristicSuccess == true) {
                        Log.d("SendViewModel", "여기 왔니")
                        uwbCommunicator.createRanging(address, channel, callback)

                        // 화면 전환
                        goScreen(ScreenType.SENDLOADING)
                    }
                }
            }
        }
    }

    fun DistanceMeasured(distance: Float) {
        println("거리 측정 : ${distance}")

        // 거리 측정 로직 처리
        if (distance < 5) {
            measurementCount++
        } else {
            measurementCount = 0  // 거리 벗어나면 카운트 초기화
        }

        if (measurementCount >= 30) {
            uwbCommunicator.destroyRanging()
            transferPoint()
        }

        // 10cm 이상 거리에서 타임아웃 처리
        timeoutHandler.postDelayed({
            if (distance > 10) {
                uwbCommunicator.destroyRanging()
                //completeLogin()
            }
        }, 3000)
    }

    /////////////////////////// Loading ///////////////////////////

    // 서버에 전송

    // 테스트용 코드

    private val fcmMessageRepository = FCMRepository()

    fun sendMessage(senderId: String, receiverId: String, content: String) {
        println("메시지 전송을 시작합니다.")
        fcmMessageRepository.saveMessage(senderId, receiverId, content)
    }

    @SuppressLint("MissingPermission")
    fun transferPoint() {
        CoroutineScope(Dispatchers.IO).launch {

            val transferRequest =
                TransferRequest(SenderId, ReceiverId!!.toLong(), point.value.toLong())

            // 송금
            val response = repository.transfer(transferRequest)

            withContext(Dispatchers.Main) {
                if (response.success) {

                    // 상대정보 받아오기
                    val partnerUserResponse = repository.getUserInfo(ReceiverId!!.toLong())

                    if (partnerUserResponse.success) {
                        _partnerUserName.value = partnerUserResponse.data?.name!!
                    }

                    val currentBalance = sharedPreferences.getLong("balance", 0)
                    val newBalance = currentBalance - point.value.toLong()

                    editor.putLong("balance", newBalance)
                    editor.apply()

                    _balance.value = newBalance

                    val myFcmToken = sharedPreferences.getString("fcmToken", null)
                    val toFcmToken = partnerUserResponse.data?.fcmtoken
                    val name = sharedPreferences.getString("name", null)

                    sendMessage(
                        myFcmToken!!,
                        toFcmToken!!,
                        "${name}님 지갑에서\n${point.value} 포인트를\n받았습니다."
                    )

                    goScreen(ScreenType.SENDCONFIRM)
                    bleScanner.stop()
                } else {
                }
            }
        }
    }

    /////////////////////////// Confirm ///////////////////////////
}
