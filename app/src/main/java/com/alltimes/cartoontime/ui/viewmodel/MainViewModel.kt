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

    /////////////////////////// Main ///////////////////////////

    private val _accelerometerData = MutableLiveData<AccelerometerDataModel>()
    val accelerometerData: LiveData<AccelerometerDataModel> get() = _accelerometerData

    fun updateAccelerometerData(newData: AccelerometerDataModel) {
        _accelerometerData.value = newData
        // 방향 판단 로직 추가
        val isFacingUp = newData.z > 0 // 예를 들어 Z 축이 0보다 큰 경우
        val orientation = if (isFacingUp) "Facing Up" else "Facing Down"
    }

    fun onSendButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SEND)
    }

    fun onReceiveButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.RECEIVE)
    }
}