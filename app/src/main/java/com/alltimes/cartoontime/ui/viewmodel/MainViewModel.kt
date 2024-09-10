package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.AccelerometerDataModel
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo

class MainViewModel(private val context: Context) : ViewModel() {

    /////////////////////////// 공용 ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    /////////////////////////// Boot ///////////////////////////

    fun onLoginClick() {
        // 로그인 처리 로직
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.MAIN)
    }

    fun onSignUpClick() {
        // 회원가입 처리 로직
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SIGNUP)
    }

    /////////////////////////// Main ///////////////////////////

    private val _accelerometerData = MutableLiveData<AccelerometerDataModel>()
    val accelerometerData: LiveData<AccelerometerDataModel> get() = _accelerometerData

    fun updateAccelerometerData(newData: AccelerometerDataModel) {
        _accelerometerData.value = newData
        // 방향 판단 로직 추가
        val isFacingUp = newData.z > 0 // 예를 들어 Z 축이 0보다 큰 경우
        val orientation = if (isFacingUp) "Facing Up" else "Facing Down"
        println("Orientation: $orientation")
    }

    fun onSendButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SEND)
    }

    fun onReceiveButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.RECEIVE)
    }
}