package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.common.NumpadAction
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.remote.ChargeRequest
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.repository.UserRepository
import com.alltimes.cartoontime.ui.handler.NumPadClickHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class BootViewModel(private val context: Context) : ViewModel(), NumpadAction {

    /////////////////////////// 공용 ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Editor 객체를 가져옵니다.
    val editor = sharedPreferences.edit()

    // 서버 통신 관련 변수
    private val repository = UserRepository(RetrofitClient.apiService)

    /////////////////////////// Boot ///////////////////////////

    fun onLoginClick() {
        // 로그인 처리 로직
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SIGNUP)
    }

    /////////////////////////// Login ///////////////////////////

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // ViewModel에서 상태를 직접 접근할 수 있도록
    val password: StateFlow<String> get() = numPadClickHandler.password

    fun authenticationSuccess() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.MAIN)

        println("인증 성공입니다 `~~~")

        // 로그인하면서 서버로부터 유저 정보 받아와서 다시 저장.
        CoroutineScope(Dispatchers.IO).launch {
            // userId와 amount를 ChargeRequest 객체에 담아서 전달
            val userId = sharedPreferences.getLong("userId", -1L)

            // API 호출
            val response = repository.getUserInfo(userId)

            println("response: $response")

            // 응답 처리
            if (response.success) {

                editor?.putLong("balance", response.data?.currentMoney!!)
                editor?.putLong("userId", response.data?.id!!)
                editor?.putString("userName", response.data?.username!!)
                editor?.putString("name", response.data?.name!!)

                editor.apply()

            }
        }
    }

    private val numPadClickHandler: NumPadClickHandler by lazy {
        NumPadClickHandler(
            context = context,
            onPasswordComplete = { password: String ->
                val userPassword = sharedPreferences.getString("password", null)
                if (userPassword == password) {
                    authenticationSuccess()
                } else {
                    numPadClickHandler.clearPassword()
                    showPasswordError()
                }
            }
        )
    }

    override fun onClickedButton(type: Int) {
        numPadClickHandler.onClickedButton(type)
    }

    private fun showPasswordError() {
        Toast.makeText(context, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }
}