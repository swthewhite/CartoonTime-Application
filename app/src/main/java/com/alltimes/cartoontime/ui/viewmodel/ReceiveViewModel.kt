package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
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
import com.alltimes.cartoontime.data.repository.FCMRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiveViewModel(private val context: Context) : ViewModel(), MessageListener {

//class ReceiverViewModel(application: Application) : AndroidViewModel(application) {

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

    var inputEnable: Boolean = true

    private val _balance = MutableStateFlow(sharedPreferences.getLong("balance", 0L))
    val balance: StateFlow<Long> = _balance

    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }

    val fcmRepository = FCMRepository(this)

    init {
        val fcmToekn = sharedPreferences.getString("fcmToken", "") ?: ""
        println("receiveViewModel FCM Token: $fcmToekn")
        fcmRepository.listenForMessages(fcmToekn)
    }

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    override fun onMessageReceived(message: FcmMessage) {
        println("메시지 수신 완료: $message")
        if (message.content.contains("포인트")) {
            // 특정 동작 수행
            _content.value = message.content
            println("포인트 입금 메시지 수신: $message")

            // 메인 스레드에서 goScreen 호출
            viewModelScope.launch {
                goScreen(ScreenType.RECEIVECONFIRM)
            }
        }
    }

    /////////////////////////// Description ///////////////////////////


    /////////////////////////// Loading ///////////////////////////


    /////////////////////////// Confirm ///////////////////////////

    private val bleServerViewModel: BLEServerViewModel = BLEServerViewModel(context)

    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    fun onButtonClick() {
        println("서버 시작 ~~~~~")
        // mode setting
        bleServerViewModel.setMode(true)
        // partnercheck

        bleServerViewModel.onButtonClick()
    }
}
