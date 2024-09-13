package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContentScope
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.AccelerometerDataModel
import com.alltimes.cartoontime.data.model.Cartoon
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

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

    // MutableStateFlow로 balance 값을 관리
    private val _balance = MutableStateFlow(sharedPreferences.getInt("balance", 8000))
    val balance: StateFlow<Int> = _balance

    // 서버 통신 관련 변수

    /////////////////////////// Main ///////////////////////////

    private val _state = MutableStateFlow("입실 전")
    val state: StateFlow<String> get() = _state

    private val _enteredTime = MutableStateFlow("2024-09-11 11:11")
    val enteredTime: StateFlow<String> get() = _enteredTime

    private val _usedTime = MutableStateFlow("1시간 10분")
    val usedTime: StateFlow<String> get() = _usedTime

    fun onSendButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SEND)
    }

    fun onReceiveButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.RECEIVE)
    }

    fun onBookRecommendButtonClick() {
        _screenNavigationTo.value = ScreenNavigationTo(ScreenType.BOOKRECOMMEND)
    }
    
    fun onLogin() {
        _state.value = "입실 완료"
    }

    fun serverConnect() {
        // 서버와 연결해서 값을 받아오는 함수
        // 지속적으로 호출되어야 하나 ?
        // state, enteredTime, usedTime, balance 값이 변경되어야 함
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

}