package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.common.NumpadAction
import com.alltimes.cartoontime.common.PointpadAction
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.handler.NumPadClickHandler
import com.alltimes.cartoontime.ui.handler.PointPadClickHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChargeViewModel(private val context: Context) : ViewModel(), NumpadAction, PointpadAction {

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

    override fun onPointClickedButton(type: Int) {
        pointPadClickHandler.onClickedButton(type, balance.value)
    }

    val point: StateFlow<String> get() = pointPadClickHandler.point

    // 눌리는 버튼에 대한 구현
    private val pointPadClickHandler: PointPadClickHandler by lazy {
        PointPadClickHandler(
            context = context,
            isPointExceeded = {
                // 천만 이하만 충전 가능
                if (point.value.toInt() > 10000000) {
                    pointPadClickHandler.setPoint("10000000")
                    showPointError()
                }
            }
        )
    }

    private fun showPointError() {
        Toast.makeText(context, "충전 가능한 포인트가 초과되었습니다", Toast.LENGTH_SHORT).show()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }


    /////////////////////////// PasswordInput ///////////////////////////

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
                    goScreen(ScreenType.CHARGECONFIRM)
                } else {
                    numPadClickHandler.clearPassword()
                    showPasswordError()
                }
            }
        )
    }

    private fun showPasswordError() {
        Toast.makeText(context, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
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