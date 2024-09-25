package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
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
import com.alltimes.cartoontime.data.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    init {
        val fcmRepository = FCMRepository(this)
        val fcmToken = sharedPreferences.getString("fcmToken", "") ?: ""
        val userId = sharedPreferences.getLong("userId", 0L)
        fcmRepository.listenForMessages(fcmToken)
        
        // 서버 api 호출
        // 인증 코드 요청
        CoroutineScope(Dispatchers.IO).launch {
            val fcmRequest = FCMRequest(userId, fcmToken)

            val response = repository.saveFcmToken(fcmRequest)
        }
    }

    override fun onMessageReceived(message: FcmMessage) {
        println("메시지 수신 완료: $message")
        if (message.content.contains("입퇴실")) {
            // 특정 동작 수행
            println("입퇴실 메시지 수신: $message")
            onKioskLoadingCompleted()
        }
    }

    private val fcmMessageRepository = FCMRepository()

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
            val toFcmToken = toUser.data?.fcmToken

            sendMessage(myFcmToken!!, toFcmToken!!, "입퇴실완료")
        }

    }

    /////////////////////////// Main ///////////////////////////

    // 현재 시각을 가져오는 함수
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    // 입실 시간과 현재 시간의 차이를 분 단위로 계산하는 함수
    private fun calculateUsedTime(enteredTime: String?, currentTime: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val enteredDate = sdf.parse(enteredTime)
        val currentDate = sdf.parse(currentTime)

        // 시간 차이를 분 단위로 계산
        return (currentDate.time - enteredDate.time) / (1000 * 60)
    }

    // 요금을 계산하는 함수 (예: 분당 100원)
    // 서버에서 해결해줘야하는 부분 ?
    private fun calculateCharge(usedTimeMinutes: Long): Int {
        val chargePerMinute = 100  // 분당 요금
        return (usedTimeMinutes * chargePerMinute).toInt()
    }

    // BleScanner 변수
    private val bleScannerViewModel: BLEScannerViewModel = BLEScannerViewModel(context)
    val uiState: StateFlow<UIStateModel> = bleScannerViewModel.uiState

    // 각속도 측정 후 입퇴실 진행
    fun onLoginOut() {
        if (_state.value == "입실 전") _state.value = "입실 중"
        else _state.value = "퇴실 중"

        bleScannerViewModel.setMode(false)
        bleScannerViewModel.startScanningAndConnect()
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

    fun onClickedHome() {
        _screenNavigationTo.value = ScreenNavigationTo(ScreenType.MAIN)
    }

    fun onClickedCartoonDetail() {
        _screenNavigationTo.value = ScreenNavigationTo(ScreenType.BOOKDETAIL)
    }

    fun fetchCartoons(category: String) {
        // 서버 통신 필요

        if (category == "사용자 취향 만화") {
            _cartoons.value = listOf(
                Cartoon("만화 1", "작가 1", "액션", "https://example.com/cover1.jpg", "F"),
                Cartoon("만화 2", "작가 2", "판타지", "https://example.com/cover2.jpg", "A"),
                Cartoon("만화 3", "작가 3", "무협", "https://example.com/cover3.jpg", "B"),
                Cartoon("만화 4", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                // 추가 데이터...
            )
        } else if (category == "베스트 셀러 만화") {
            _cartoons.value = listOf(
                Cartoon("만화 4", "작가 1", "액션", "https://example.com/cover1.jpg", "F"),
                Cartoon("만화 3", "작가 2", "판타지", "https://example.com/cover2.jpg", "A"),
                Cartoon("만화 2", "작가 3", "무협", "https://example.com/cover3.jpg", "B"),
                Cartoon("만화 1", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                // 추가 데이터...
            )
        } else if (category == "오늘의 추천 만화") {
            _cartoons.value = listOf(
                Cartoon("만화 2", "작가 1", "액션", "https://example.com/cover1.jpg", "F"),
                Cartoon("만화 4", "작가 2", "판타지", "https://example.com/cover2.jpg", "A"),
                Cartoon("만화 1", "작가 3", "무협", "https://example.com/cover3.jpg", "B"),
                Cartoon("만화 3", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                // 추가 데이터...
            )
        }
    }

    /////////////////////////// Confirm ///////////////////////////

    // 결제 완료
    fun onConfirmButtonClick() {
        // 입실 전 상태로 변경
        _state.value = "입실 전"
        editor.putString("state", "입실 전")
        editor.apply()

        // 요금 정산
        _balance.value -= _charge.value
        editor.putLong("balance", _balance.value)
        editor.apply()

        _charge.value = -1L

        goScreen(ScreenType.MAIN)
    }

}