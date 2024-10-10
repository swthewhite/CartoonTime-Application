package com.alltimes.cartoontime.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
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
import kotlinx.coroutines.delay
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

    // 로딩 다이얼로그
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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

    fun initializePassword() {
        numPadClickHandler.clearPassword()
    }

    private val _redirectUrl = MutableLiveData<String?>()
    val redirectUrl: LiveData<String?> get() = _redirectUrl

    fun handleRedirectUrl(url: String) {
        _redirectUrl.postValue(url)
    }

    fun pointCharge() {
        // 충전 API 호출
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            // userId와 amount를 ChargeRequest 객체에 담아서 전달
            val userId = sharedPreferences.getLong("userId", -1L)
            val chargeRequest = ChargeRequest(
                cid = "ct",
                partnerUserId = userId,
                itemName = "카툰타임 ${point.value.toInt()}원 충전",
                totalAmount = point.value.toLong()
            )

            // API 호출
            val response = try {
                repository.charge(chargeRequest)
            } catch (e: Exception) {
                // 에러 처리
                _isLoading.value = false
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "포인트 충전에 실패했습니다", Toast.LENGTH_SHORT).show()
                    initializePassword()
                }
                return@launch
            }

            // 응답 처리 - 리다이렉트 URL 받기
            if (response?.success == true) {
                _isLoading.value = false

                println("response.data: ${response.data}")

                // 리다이렉트 URL을 받았다면 WebView 또는 외부 브라우저로 전환
                _redirectUrl.postValue(response.data) // 백그라운드 스레드에서 postValue 사용

                withContext(Dispatchers.Main) {
                    // WebView를 사용하거나 외부 브라우저로 리다이렉트 URL을 열기
                    _redirectUrl.value?.let { handleRedirectUrl(it) }
                    //openKakaoPayWebView(redirectUrl.toString())
                }
            } else {
                // 에러 처리
                handlePaymentError(response?.message ?: "포인트 충전에 실패했습니다")
            }
        }
    }

    suspend fun handlePaymentResult() {

        Log.d("ChargePasswordInputScreen", "OK 드가자")

        // 화면 전환 및 로딩 시작
        withContext(Dispatchers.Main) {
            goScreen(ScreenType.CHARGECONFIRM)
            _isLoading.value = true
        }

        // 결제 완료 여부 확인 작업을 백그라운드에서 수행
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                // 결제 완료 여부 확인
                val response = try {
                    repository.checkCharge(sharedPreferences.getLong("userId", -1L))
                } catch (e: Exception) {
                    // 에러 처리
                    handlePaymentError("결제에 실패했습니다")
                    return@launch
                }

                Log.d("ChargePasswordInputScreen", "response.data: ${response.data}")

                if (response.data) {
                    // 결제가 아직 완료되지 않음, 1초 후 다시 시도
                    Log.d("ChargePasswordInputScreen", "블록체인 진행중: ${response.message}")
                    delay(1000) // 1초 대기
                } else {
                    // 결제 완료, 메인 스레드에서 로딩 상태 종료
                    Log.d("ChargePasswordInputScreen", "결제 완료: ${response.message}")
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                    }
                    return@launch // 루프 종료
                }
            }
        }
    }

    suspend fun handlePaymentError(error: String){
        _isLoading.value = false
        withContext(Dispatchers.Main) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            initializePassword()
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