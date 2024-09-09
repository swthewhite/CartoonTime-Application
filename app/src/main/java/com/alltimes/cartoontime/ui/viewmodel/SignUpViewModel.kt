package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.data.model.NavigationTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SignUpViewModel(private val context: Context?) : ViewModel() {
    private val _phoneNumber = MutableStateFlow(TextFieldValue())
    val phoneNumber: StateFlow<TextFieldValue> = _phoneNumber

    private val _verificationCode = MutableStateFlow(TextFieldValue())
    val verificationCode: StateFlow<TextFieldValue> = _verificationCode

    private val _name = MutableStateFlow(TextFieldValue())
    val name: StateFlow<TextFieldValue> = _name

    private val _isPhoneNumberEnable = MutableStateFlow(true)
    val isPhoneNumberEnable: StateFlow<Boolean> = _isPhoneNumberEnable

    private val _isVerificationCodeVisible = MutableStateFlow(false)
    val isVerificationCodeVisible: StateFlow<Boolean> = _isVerificationCodeVisible

    private val _isVerificationCodeCorret = MutableStateFlow(false)
    val isVerificationCodeCorrect: StateFlow<Boolean> = _isVerificationCodeCorret

    private val _isNameCorrect = MutableStateFlow(false)
    val isNameCorrect: StateFlow<Boolean> = _isNameCorrect

    private val _isSubmitButtonEnabled = MutableStateFlow(false)
    val isSubmitButtonEnabled: StateFlow<Boolean> = _isSubmitButtonEnabled

    private val _navigationTo = MutableLiveData<NavigationTo>()
    val navigationTo: LiveData<NavigationTo> get() = _navigationTo

    fun onLogout() {
        // 로그아웃 처리 로직
        _navigationTo.value = NavigationTo(ActivityType.FINISH)
    }

    private fun checkSubmitButtonState() {
        // 인증번호 검사 로직
        context?.let {
            if (_verificationCode.value.text.length == 6) {
                if (_verificationCode.value.text == "123456") {
                    _isVerificationCodeCorret.value = true
                } else {
                    _isVerificationCodeCorret.value = false
                    Toast.makeText(it, "인증번호가 다릅니다.", Toast.LENGTH_SHORT).show()
                    val vibrator = it.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (vibrator.hasVibrator()) {
                        val vibrationEffect =
                            VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                        vibrator.vibrate(vibrationEffect)
                    }

                    _verificationCode.value = TextFieldValue()
                }
            } else if (_verificationCode.value.text.length > 6) {
                Toast.makeText(it, "인증번호는 6자리 입니다.", Toast.LENGTH_SHORT).show()
                val vibrator = it.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) {
                    val vibrationEffect =
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                }

                _verificationCode.value = TextFieldValue()
            }

            // 이름 검사 로직
            if (_name.value.text.length >= 2) {
                _isNameCorrect.value = true
            } else {
                _isNameCorrect.value = false
            }

            // 전체적인 버튼 활성화 로직
            _isSubmitButtonEnabled.value = _isVerificationCodeCorret.value && _isNameCorrect.value
        }
    }

    fun onSubmit() {
        // 인증 처리 로직을 구현
        _navigationTo.value = NavigationTo(ActivityType.PASSWORDSETTING)
    }

    fun onRequestVerificationCode() {
        context?.let {
            // TextFieldValue의 text 프로퍼티를 사용하여 문자열을 얻어옴
            val phoneNumberString = phoneNumber.value.text
            val isValidPhoneNumber = phoneNumberString.matches(Regex("^010\\d{8}$"))

            if (isValidPhoneNumber) {
                _isVerificationCodeVisible.value = true
                _isPhoneNumberEnable.value = false
            } else {
                Toast.makeText(it, "전화번호 형식을 지켜주세요", Toast.LENGTH_SHORT).show()

                // 전화번호 입력 필드를 비웁니다.
                _phoneNumber.value = TextFieldValue()
            }
        }
    }

    fun onPhoneNumberChange(newValue: TextFieldValue) {
        _phoneNumber.value = newValue
        checkSubmitButtonState()
    }

    fun onVerificationCodeChange(newValue: TextFieldValue) {
        _verificationCode.value = newValue
        checkSubmitButtonState()
    }

    fun onNameChange(newValue: TextFieldValue) {
        _name.value = newValue
        checkSubmitButtonState()
        // 이름은 아무렇게나
        if (_name.value.text.length >= 2) {
            _isNameCorrect.value = true
        }
        else {
            _isNameCorrect.value = false
        }
    }
}
