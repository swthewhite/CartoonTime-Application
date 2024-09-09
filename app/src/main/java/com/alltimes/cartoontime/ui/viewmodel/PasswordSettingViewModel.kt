package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.data.model.NavigationTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PasswordSettingViewModel(private val context: Context) : ViewModel() {

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _passwordCheck = MutableStateFlow(false)
    val passwordCheck: StateFlow<Boolean> = _passwordCheck

    var inputEnable: Boolean = true
    var PassWord = ""

    private val _navigationTo = MutableLiveData<NavigationTo>()
    val navigationTo: LiveData<NavigationTo> get() = _navigationTo


    // 눌리는 버튼에 대한 구현
    fun onClickedButton(type: Int) {

        if (!inputEnable) return

        if (type == -1) _password.value = _password.value.dropLast(1)
        else _password.value += type.toString()


        // 6자리 모두 입력시 한번 더 체크
        if (password.value.length == 6) {
            if (!passwordCheck.value) {
                _passwordCheck.value = true
                PassWord = password.value
                _password.value = ""
            }
            else
            {
                inputEnable = false
                checkPassword()
            }
        }
    }

    // 입력된 비밀번호 체크
    private fun checkPassword() {
        println("비밀번호 체크 중")
        context?.let {
            if (_password.value == PassWord) {
                _navigationTo.value = NavigationTo(ActivityType.NAVERLOGIN)

                Toast.makeText(it, "동일합니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(it, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
                val vibrator = it.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) {
                    val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                }

                inputEnable = true
                _passwordCheck.value = false
                PassWord = ""
                _password.value = ""
            }
        }
    }

}