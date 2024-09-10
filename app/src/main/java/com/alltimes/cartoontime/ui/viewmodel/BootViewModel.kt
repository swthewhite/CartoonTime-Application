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
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class BootViewModel(private val context: Context) : ViewModel() {

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

    /////////////////////////// Boot ///////////////////////////

    fun onLoginClick() {
        // 로그인 처리 로직
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SIGNUP)
    }

    fun onSignUpClick() {
        // 회원가입 처리 로직
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SIGNUP)
    }

    /////////////////////////// Login ///////////////////////////

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    var inputEnable: Boolean = true

    fun authenticationSuccess() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.MAIN)
    }

    fun onClickedButton(type: Int) {
        context?.let {
            if (!inputEnable) return

            if (type == -1) _password.value = _password.value.dropLast(1)
            else _password.value += type.toString()


            // 6자리 모두 입력시 비밀번호와 비교
            if (password.value.length == 6) {
                val userPassword = sharedPreferences.getString("password", null)
                if (userPassword == password.value) {
                    authenticationSuccess()
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
}