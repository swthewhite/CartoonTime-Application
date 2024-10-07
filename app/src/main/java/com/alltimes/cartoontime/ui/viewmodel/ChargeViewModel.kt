package com.alltimes.cartoontime.ui.viewmodel

import android.app.Application
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
import com.alltimes.cartoontime.data.remote.ChargeRequest
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.repository.UserRepository
import com.alltimes.cartoontime.ui.handler.NumPadClickHandler
import com.alltimes.cartoontime.ui.handler.PointPadClickHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChargeViewModel(application: Application, private val context: Context) : BaseViewModel(application), NumpadAction, PointpadAction {

    /////////////////////////// 공용 ///////////////////////////

    val userName = sharedPreferences.getString("name", "")

    // MutableStateFlow로 balance 값을 관리
    private val _balance = MutableStateFlow(sharedPreferences.getLong("balance", 0L))
    val balance: StateFlow<Long> = _balance

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
                val currentPoint = point.value.toIntOrNull() ?: 0 // 안전하게 변환

                // 천만 이하만 충전 가능
                if (currentPoint > 10000000) {
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
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }


    /////////////////////////// PasswordInput ///////////////////////////

    override fun onClickedButton(type: Int) {
        numPadClickHandler.onClickedButton(type)
    }

    val password: StateFlow<String> get() = numPadClickHandler.password

    fun pointCharge() {
        // 충전 api 호출
        CoroutineScope(Dispatchers.IO).launch {
            // userId와 amount를 ChargeRequest 객체에 담아서 전달
            val userId = sharedPreferences.getLong("userId", -1L)
            val chargeRequest =
                ChargeRequest(cid = "ct", partnerUserId = userId, itemName = "카툰타임 ${point.value.toInt()}원 충전", totalAmount = point.value.toLong())

            // API 호출
            val response = try {
                repository.charge(chargeRequest)
            } catch (e: Exception) {
                // 에러처리
                null
            }

            // 응답 처리
            if (response?.success == true) {
                // 메인 스레드에서 값 변경 및 UI 업데이트
                withContext(Dispatchers.Main) {
                    val currentBalance = sharedPreferences.getLong("balance", 0)
                    val newBalance = currentBalance + point.value.toLong()

                    editor.putLong("balance", newBalance)
                    editor.apply()

                    _balance.value = newBalance

                    goScreen(ScreenType.CHARGECONFIRM)
                }
            }
        }
    }

    private val numPadClickHandler: NumPadClickHandler by lazy {
        NumPadClickHandler(
            context = context,
            onPasswordComplete = { password: String ->
                val userPassword = sharedPreferences.getString("password", null)
                if (userPassword == password) {
                    pointCharge()
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
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }

    /////////////////////////// Confirm ///////////////////////////
}