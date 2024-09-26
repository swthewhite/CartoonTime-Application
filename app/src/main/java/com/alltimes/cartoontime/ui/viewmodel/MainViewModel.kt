package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.common.MessageListener
import com.alltimes.cartoontime.data.model.Cartoon
import com.alltimes.cartoontime.data.model.FcmMessage
import com.alltimes.cartoontime.data.model.UIStateModel
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.remote.FCMRequest
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.repository.FCMRepository
import com.alltimes.cartoontime.data.repository.UserInfoUpdater
import com.alltimes.cartoontime.data.repository.UserRepository
import com.alltimes.cartoontime.utils.AccelerometerManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    val name = sharedPreferences.getString("name", "")
    val fcmToken = sharedPreferences.getString("fcmToken", "")
    val userId = sharedPreferences.getLong("userId", 0L)

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

    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }

    // 서버 통신 관련 변수
    private val repository = UserRepository(RetrofitClient.apiService)

    private val fcmRepository = FCMRepository(this)

    var isFCMActive = false

    init {
        fcmRepository.listenForMessages(fcmToken!!)
        isFCMActive = true

        // 서버 api 호출
        CoroutineScope(Dispatchers.IO).launch {
            val fcmRequest = FCMRequest(userId, fcmToken)

            val response = try{
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

    fun UpdateUserInfo() {
        userInfoUpdater.updateUserInfo(sharedPreferences.getLong("userId", -1L))
    }

    fun onPuaseAll() {
        accelerometerStop()

        isFCMActive = false
    }

    fun onResumeAll() {
        UpdateUserInfo()

        isFCMActive = true
    }

    /////////////////////////// Main ///////////////////////////

    // 각속도 측정
    private lateinit var accelerometerManager: AccelerometerManager
    private var accelerometerCount by Delegates.notNull<Int>()
    private var accelerometerIsCounting = true // 1분 동안 카운팅 방지용 플래그

    private val fcmMessageRepository = FCMRepository()

    // FCM 메시지 수신
    override fun onMessageReceived(message: FcmMessage) {
        if (!isFCMActive) return
        println("MAIN 메시지 수신 완료: $message")
        if (message.content.contains("입퇴실")) {
            // 특정 동작 수행
            onKioskLoadingCompleted()
        }
        else if (message.content.contains("원")) {
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

    fun sendMessage(senderId: String, receiverId: String, content: String) {
        fcmMessageRepository.saveMessage(senderId, receiverId, content)
    }

    fun testSendToggleMessage() {
        CoroutineScope(Dispatchers.IO).launch {
            val fromUserId = sharedPreferences.getLong("userId", -1L)
            val toUserId = 1L
            // 상대 정보 받아오기
            val toUser = repository.getUserInfo(toUserId)

            val myFcmToken = sharedPreferences.getString("fcmToken", null)
            val toFcmToken = toUser.data?.fcmtoken

            sendMessage(myFcmToken!!, toFcmToken!!, "입퇴실완료")
        }

    }

    // 현재 시각을 가져오는 함수
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    // BleScanner 변수
    //private val bleScannerViewModel: BLEScannerViewModel = BLEScannerViewModel(context)
    //val uiState: StateFlow<UIStateModel> = bleScannerViewModel.uiState

    // 각속도 측정 후 입퇴실 진행
    fun onLoginOut() {
        if (_state.value == "입실 전") _state.value = "입실 중"
        else _state.value = "퇴실 중"

        //bleScannerViewModel.setMode(false)
        //bleScannerViewModel.startScanningAndConnect()
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

    private val _cartoons = MutableStateFlow<List<Cartoon>>(emptyList())
    val cartoons: StateFlow<List<Cartoon>> = _cartoons

    private val _clickedCartoon = MutableStateFlow<Cartoon>(Cartoon("", "", "", "", ""))
    val clickedCartoon: StateFlow<Cartoon> = _clickedCartoon

    private val _category = MutableStateFlow("사용자 취향 만화")
    val category: StateFlow<String> get() = _category

    fun bookRecommendInit() {
        fetchCartoons(_category.toString())
    }

    fun onClickedCategory(category: String) {
        _category.value = category
    }

    fun onClickedCartoon(cartoon: Cartoon) {
        if (_clickedCartoon.value == cartoon) {
            _clickedCartoon.value = Cartoon("", "", "", "", "")
        } else {
            _clickedCartoon.value = cartoon
        }
    }

    fun fetchCartoons(category: String) {
        // 서버 통신 필요

        if (category == "사용자 취향 만화") {
            _cartoons.value = listOf(
                Cartoon("만화 1", "작가 1", "액션", "https://example.com/cover1.jpg", "F"),
                Cartoon("만화 2", "작가 2", "판타지", "https://example.com/cover2.jpg", "A"),
                Cartoon("만화 3", "작가 3", "무협", "https://example.com/cover3.jpg", "B"),
                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),

                // 추가 데이터...
            )
        } else if (category == "베스트 셀러 만화") {
            _cartoons.value = listOf(
                Cartoon("만화 4", "작가 1", "액션", "https://example.com/cover1.jpg", "F"),
                Cartoon("만화 3", "작가 2", "판타지", "https://example.com/cover2.jpg", "A"),
                // 추가 데이터...
            )
        } else if (category == "오늘의 추천 만화") {
            _cartoons.value = listOf(
                Cartoon("만화 2", "작가 1", "액션", "https://example.com/cover1.jpg", "F"),
                Cartoon("만화 4", "작가 2", "판타지", "https://example.com/cover2.jpg", "A"),
                Cartoon("만화 1", "작가 3", "무협", "https://example.com/cover3.jpg", "B"),
                // 추가 데이터...
            )
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