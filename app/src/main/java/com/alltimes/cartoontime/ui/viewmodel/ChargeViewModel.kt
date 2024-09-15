package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChargeViewModel(private val context: Context) : ViewModel() {

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

    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }

    // 서버 통신 관련 변수

    /////////////////////////// PointInput ///////////////////////////

    private val _point = MutableStateFlow("")
    val point: StateFlow<String> get() = _point

    // 눌리는 버튼에 대한 구현
    fun onPointClickedButton(type: Int) {
        context?.let {
            if (type == -1) {
                if (_point.value != "") _point.value = _point.value.dropLast(1)
                return
            }
            else if (type == -2) _point.value += "00"
            else _point.value += type.toString()

            // 천만 이하만 충전 가능
            if (_point.value.toInt() > 10000000) {
                _point.value = 10000000.toString()
                // 경고 느낌으로 진동 한번
                val vibrator = it.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) {
                    val vibrationEffect =
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                }
            }
        }
    }


    /////////////////////////// PasswordInput ///////////////////////////

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> get() = _password

    fun onPasswordClickedButton(type: Int) {
        context?.let {
            //if (!inputEnable) return

            if (type == -1) {
                if (_password.value != "") _password.value = _password.value.dropLast(1)
                return
            }
            else _password.value += type.toString()


            // 6자리 모두 입력시 비밀번호와 비교
            if (password.value.length == 6) {
                val userPassword = sharedPreferences.getString("password", null)
                if (userPassword == password.value) {
                    goScreen(ScreenType.CONFIRM)
                } else {
                    _password.value = ""

                    Toast.makeText(it, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
                    val vibrator = it.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (vibrator.hasVibrator()) {
                        val vibrationEffect =
                            VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                        vibrator.vibrate(vibrationEffect)
                    }

                }
            }
        }
    }

    /////////////////////////// Confirm ///////////////////////////

    fun onCharge(chargePoint : String) {
        val currentBalance = sharedPreferences.getInt("balance", 0)
        val newBalance = currentBalance + chargePoint.toInt()
        editor.putInt("balance", newBalance)
        editor.apply()

        _balance.value = newBalance
    }
}