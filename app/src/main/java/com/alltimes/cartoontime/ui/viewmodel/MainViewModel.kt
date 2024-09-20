package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.Cartoon
import com.alltimes.cartoontime.data.model.UIStateModel
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(private val context: Context) : ViewModel() {

    /////////////////////////// 공용 ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    // SharedPreferences 객체를 가져옵니다.
    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Editor 객체를 가져옵니다.
    val editor = sharedPreferences.edit()

    val userName = sharedPreferences.getString("name", "")

    private val _state = MutableStateFlow(sharedPreferences.getString("state", "입실 전"))
    val state: MutableStateFlow<String?> = _state

    // MutableStateFlow로 balance 값을 관리
    private val _balance = MutableStateFlow(sharedPreferences.getInt("balance", 8000))
    val balance: StateFlow<Int> = _balance

    private val _enteredTime = MutableStateFlow(sharedPreferences.getString("enteredTime", "2024-08-19 09:00:00"))
    val enteredTime: MutableStateFlow<String?> = _enteredTime

    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }

    // 서버 통신 관련 변수

    /////////////////////////// Main ///////////////////////////

    fun onSendButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SEND)
    }

    fun onReceiveButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.RECEIVE)
    }

    fun onChargeButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.CHARGE)
    }

    fun onBookRecommendButtonClick() {
        _screenNavigationTo.value = ScreenNavigationTo(ScreenType.BOOKRECOMMEND)
    }

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
    private fun calculateCharge(usedTimeMinutes: Long): Int {
        val chargePerMinute = 100  // 분당 요금
        return (usedTimeMinutes * chargePerMinute).toInt()
    }

    private val bleScannerViewModel: BLEScannerViewModel = BLEScannerViewModel(context)
    val uiState: StateFlow<UIStateModel> = bleScannerViewModel.uiState

    fun onLoginOut() {
        _state.value = "입실 중"
        bleScannerViewModel.setMode(false)
        bleScannerViewModel.startScanningAndConnect()
    }

    fun serverConnect() {
        // 서버와 연결해서 값을 받아오는 함수
        // 지속적으로 호출되어야 하나 ?
        // state, enteredTime, usedTime, balance 값이 변경되어야 함
    }

    fun onKioskLoadingCompleted() {

        println("onLoginOut")
        println("state: ${_state.value}")
        println("get state : ${sharedPreferences.getString("state", "입실 전")}")

        val currentState = sharedPreferences.getString("state", "입실")
        val currentTime = getCurrentTime()  // 현재 시각을 가져오는 함수

        if (currentState == "입실 전") {
            // 입실 완료 상태로 변경
            _state.value = "입실 완료"
            editor.putString("state", "입실 완료")
            editor.apply()

            // 현재 시각을 기록
            _enteredTime.value = currentTime
            editor.putString("enteredTime", currentTime)
            editor.apply()

        } else if (currentState == "입실 완료") {

            // 입실 시 기록된 시간과 현재 시간을 바탕으로 요금 계산
            val enteredTime = sharedPreferences.getString("enteredTime", currentTime)
            val usedTimeMinutes = calculateUsedTime(enteredTime, currentTime)  // 사용 시간을 분 단위로 계산
            _charge.value = calculateCharge(usedTimeMinutes) + 50000 // 요금을 계산

            // 요금 정산 페이지로 넘어가기
            _screenNavigationTo.value = ScreenNavigationTo(ScreenType.CONFIRM)
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
        }
        else if (category == "베스트 셀러 만화") {
            _cartoons.value = listOf(
                Cartoon("만화 4", "작가 1", "액션", "https://example.com/cover1.jpg", "F"),
                Cartoon("만화 3", "작가 2", "판타지", "https://example.com/cover2.jpg", "A"),
                Cartoon("만화 2", "작가 3", "무협", "https://example.com/cover3.jpg", "B"),
                Cartoon("만화 1", "작가 4", "코믹", "https://example.com/cover4.jpg", "D"),
                // 추가 데이터...
            )
        }
        else if (category == "오늘의 추천 만화") {
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

    private val _charge = MutableStateFlow(0)
    val charge: StateFlow<Int> = _charge

    fun onConfirmButtonClick() {
        // 입실 전 상태로 변경
        _state.value = "입실 전"
        editor.putString("state", "입실 전")
        editor.apply()

        // 요금 정산
        _balance.value -= _charge.value
        editor.putInt("balance", _balance.value)
        editor.apply()

        _screenNavigationTo.value = ScreenNavigationTo(ScreenType.MAIN)
    }

}