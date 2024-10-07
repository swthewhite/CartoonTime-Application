package com.alltimes.cartoontime.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.common.MessageListener
import com.alltimes.cartoontime.data.model.fcm.FcmMessage
import com.alltimes.cartoontime.data.model.ui.SendUiState
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.model.uwb.Location
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.ble.BLEClient
import com.alltimes.cartoontime.data.network.ble.BLEScanner
import com.alltimes.cartoontime.data.network.mqtt.MqttClient
import com.alltimes.cartoontime.data.network.uwb.UWBControlee
import com.alltimes.cartoontime.data.remote.ComicResponse
import com.alltimes.cartoontime.data.remote.FCMRequest
import com.alltimes.cartoontime.data.remote.Genre
import com.alltimes.cartoontime.data.remote.Recommendation
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.remote.UserComicRecommendResponse
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
import kotlin.math.atan2
import kotlin.properties.Delegates
import kotlinx.serialization.json.Json

class MainViewModel(application: Application, private val context: Context) : BaseViewModel(application), MessageListener,
    SensorEventListener {

    /////////////////////////// 공용 ///////////////////////////

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
    private val _state = MutableStateFlow(sharedPreferences.getString("state", "입실 완료"))
    val state: MutableStateFlow<String?> = _state

    // 입실 시간
    private val _enteredTime =
        MutableStateFlow(sharedPreferences.getString("enteredTime", "2024-08-19 09:00:00"))
    val enteredTime: MutableStateFlow<String?> = _enteredTime

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

    // 휴대폰 진동
    private fun triggerVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }

    /////////////////////////// Main ///////////////////////////

    // 각속도 측정용
    private lateinit var accelerometerManager: AccelerometerManager
    private var accelerometerCount by Delegates.notNull<Int>()
    private var accelerometerIsCounting = true // 1분 동안 카운팅 방지용 플래그

    // FCM 메시지 전송
    private fun sendMessage(senderId: String, receiverId: String, content: String) {
        println("메시지 전송을 시작합니다.")
        fcmRepository.saveMessage(senderId, receiverId, content)
    }

    fun sendKioskSkip(){
        if (fcmToken != null) {
            sendMessage(fcmToken, "kiosk", "kioskskip")
        }
    }

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
        } else if (message.content.contains("만화")) {
            // "만화/{category_index}/{comic_index}"에서 category_index와 comic_index를 추출
            val pattern = Regex("만화/(\\d+)/(\\d+)")
            val matchResult = pattern.find(message.content)

            if (matchResult != null) {
                val categoryIndex = matchResult.groupValues[1].toInt()
                val comicIndex = matchResult.groupValues[2].toInt()

                // categoryIndex에 따라 카테고리 설정
                when (categoryIndex) {
                    0 -> _category.value = "사용자 취향 만화"
                    1 -> _category.value = "베스트 셀러 만화"
                    2 -> _category.value = "오늘의 추천 만화"
                    else -> _category.value = "사용자 취향 만화" // 기본값
                }

                // 카테고리 만화 목록을 불러온 후 comicIndex에 해당하는 만화 선택
                fetchCartoons(_category.value)

                CoroutineScope(Dispatchers.Main).launch {
                    // 만화 목록이 업데이트될 때까지 대기
                    cartoons.collect { cartoonList ->
                        if (cartoonList.isNotEmpty()) {
                            // comicIndex가 목록의 범위를 벗어나지 않으면 clickedCartoon 설정
                            triggerVibration(context)
                            if (comicIndex in cartoonList.indices) {
                                _clickedCartoon.value = cartoonList[comicIndex]
                                context?.let {
                                    Toast.makeText(it, "선택하신 만화로 경로를 안내합니다.", Toast.LENGTH_SHORT).show()
                                }
                                goScreen(ScreenType.BOOKNAV)
                            } else {
                                // comicIndex가 범위를 벗어나면 기본 처리
                                _clickedCartoon.value = null
                                context?.let {
                                    Toast.makeText(it, "에러발생. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
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
    private val uwbCommunicator = UWBControlee(application)

    /////////////////////////// *BLEScanner* ///////////////////////////
    private val bleScanner = BLEScanner(application)

    private val triedDevices = mutableSetOf<String>()

    private val _activeConnection = MutableStateFlow<BLEClient?>(null)
    val activeConnection: MutableStateFlow<BLEClient?> get() = _activeConnection

    // 4. BLE 스캔
    private val _uiState = MutableStateFlow(SendUiState())
    val uiState = _uiState.asStateFlow()

    private var kioskLoadingJob: Job? = null

    // 각속도 측정 후 입퇴실 진행
    fun onLoginOut() {
        triggerVibration(context)

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
            _targetLocation.value = Location(10f, 8f) // 클릭 해제 시 기본 위치로 재설정
        } else {
            _clickedCartoon.value = cartoon
            // 클릭한 만화책의 위치에 따라 목표 위치 업데이트
            _targetLocation.value = mapLocationToTarget(cartoon.location)
        }
    }

    // Recommendation을 ComicResponse로 변환하는 함수
    private fun convertRecommendationToComicResponse(recommendation: Recommendation): ComicResponse {
        return ComicResponse(
            id = recommendation.id, // 이제 recommendation의 ID를 사용
            titleEn = recommendation.titleKo, // 영어 제목이 없으므로, 한국 제목으로 설정
            titleKo = recommendation.titleKo,
            authorEn = recommendation.authorKo, // 영어 저자명도 한국 저자명으로 설정
            authorKo = recommendation.authorKo,
            location = recommendation.location,
            imageUrl = recommendation.imageUrl,
            genres = recommendation.genres.map {
                Genre(
                    id = it.id,
                    genreNameEn = it.genreNameKo, // 필요에 따라 변환
                    genreNameKo = it.genreNameKo
                )
            }
        )
    }

    fun fetchCartoons(category: String) {
        println("fetchCartoons: $category")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (category) {
                    "사용자 취향 만화" -> {
                        val recommendedComicsResponse = repository.userRecommendComics(userId)
                        println("recommendedComicsResponse: $recommendedComicsResponse")

                        // 추천 만화를 변환
                        val convertedComics = recommendedComicsResponse.map { recommendation ->
                            convertRecommendationToComicResponse(recommendation)
                        }
                        println("convertedComics: $convertedComics")
                        _cartoons.value = convertedComics
                    }
                    else -> {
                        val allComics = repository.getAllComics()
                        _cartoons.value = allComics
                    }
                }
            } catch (e: Exception) {
                println("Failed to fetch cartoons: ${e.message}")
                _cartoons.value = emptyList()
            }
        }
    }

    /////////////////////////// BookNav ///////////////////////////

    // 콜백함수로 handleMessage 등록하면서 MQTT 초기화
    private val mqttClient = MqttClient("myPos", context) { message ->
        handleMessage(message)
    }

    // 메시지가 들어왔을때 실행될 함수
    private fun handleMessage(message: String) {
        // 메시지를 JSON 형태로 파싱
        try {
            // 수신된 메시지 예시: {"x": 2.3, "y": 1.1}
            val json = Json.decodeFromString<Map<String, Float>>(message)
            val x = json["x"] ?: 0f
            val y = json["y"] ?: 0f
            _currentLocation.value = Location(x, y)  // 현재 위치 업데이트
        } catch (e: Exception) {
            println("Failed to parse message: ${e.message}")
        }
    }

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // 현재 위치
    private val _currentLocation = MutableStateFlow(Location(5f, 4f)) // 초기값
    val currentLocation: StateFlow<Location> get() = _currentLocation

    // 목표 위치
    private val _targetLocation = MutableStateFlow(Location(10f, 8f)) // 예시값
    val targetLocation: StateFlow<Location> get() = _targetLocation

    private val _direction = MutableStateFlow<Float>(0f)
    val direction: StateFlow<Float> get() = _direction

    private var accelerometerValues = FloatArray(3)
    private var magnetometerValues = FloatArray(3)
    private var gyroValues = FloatArray(3)

    // 만화책 위치에 따른 목표 위치 매핑 함수
    private fun mapLocationToTarget(location: String): Location {
        return when (location) {
            "A" -> Location(2.66f, 1.28f) // 좌표 (X, Y)
            "B" -> Location(5.32f, 1.28f)
            "C" -> Location(7.99f, 1.28f)
            "D" -> Location(3.33f, 4.64f)
            "E" -> Location(6.00f, 4.64f)
            "F" -> Location(8.66f, 4.64f)
            else -> Location(10.00f, 8.00f) // 기본값
        }
    }

    // 거리 계산 함수
    fun calculateDistance(currentLocation: Location, targetLocation: Location): Float {
        val dx = _targetLocation.value.x - _currentLocation.value.x
        val dy = _targetLocation.value.y - _currentLocation.value.y
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun initializeSensors() {
        // 센서 초기화
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerValues = event.values.clone()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magnetometerValues = event.values.clone()
            }
        }

        // 방향 계산
        calculateOrientation()
    }

    // 현재 위치와 목표 위치를 사용하여 목표 방향을 계산하는 함수
    fun calculateTargetDirection(current: Location, target: Location): Float {
        val dx = target.x - current.x
        val dy = target.y - current.y
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    private fun calculateOrientation() {
        // 가속도계와 자기계 데이터를 사용하여 방향 계산
        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val orientationValues = FloatArray(3)

        if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accelerometerValues, magnetometerValues)) {
            SensorManager.getOrientation(rotationMatrix, orientationValues)
            val azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
            _direction.value = azimuth
        }
    }

    // MQTT 연결 초기화 함수 호출
    fun initializeMQTT() {
        mqttClient.connectToMQTTBroker()
    }

    // MQTT 연결 해제 함수 호출
    fun disconnectMQTT() {
        mqttClient.disconnect()
    }


    /////////////////////////// Confirm ///////////////////////////

    fun handleChargeConfirm() {
        if (_balance.value < _charge.value) goActivity(ActivityType.CHARGE)
        else {
            // 정산 완료
            UpdateUserInfo()
            goScreen(ScreenType.MAIN)

            // 평점을 매겨주세요.
        }
    }
}