package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.common.NumpadAction
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.repository.UserRepository
import com.alltimes.cartoontime.ui.handler.NumPadClickHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(private val context: Context?) : ViewModel(), NumpadAction {

    /////////////////////////// 공용 ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    // SharedPreferences 객체를 가져옵니다.
    private val sharedPreferences: SharedPreferences?
        get() = context?.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Editor 객체를 가져옵니다.
    val editor = sharedPreferences?.edit()

    // 회원가입인가 ? 로그인인가 ?
    var isSignUp = true

    // 서버 통신 관련 변수
    private val repository = UserRepository(RetrofitClient.apiService)

    /////////////////////////// SignUp ///////////////////////////

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


    fun onLogout() {
        // 로그아웃 처리 로직
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.FINISH)
    }

    private fun checkSubmitButtonState() {
        // 인증번호 검사 로직
        // 인증번호 검사 로직
        context?.let {
            // 이름 검사 로직
            _isNameCorrect.value = _name.value.text.length >= 2
            // 전체적인 버튼 활성화 로직
            _isSubmitButtonEnabled.value = _isVerificationCodeCorret.value && _isNameCorrect.value
        }
    }

    fun onRequestVerificationCode() {
        context?.let {
            // TextFieldValue의 text 프로퍼티를 사용하여 문자열을 얻어옴
            val phoneNumberString = phoneNumber.value.text
            val isValidPhoneNumber = phoneNumberString.matches(Regex("^010\\d{8}$"))

            if (isValidPhoneNumber) {
                _isVerificationCodeVisible.value = true
                _isPhoneNumberEnable.value = false

                // 인증 코드 요청
                CoroutineScope(Dispatchers.IO).launch {
                    val response = repository.requestAuthCode(phoneNumberString)
                    if (response.success) {
                        Toast.makeText(it, response.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(it, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(it, "전화번호 형식을 지켜주세요", Toast.LENGTH_SHORT).show()

                // 전화번호 입력 필드를 비웁니다.
                _phoneNumber.value = TextFieldValue()
            }
        }
    }

    fun onVerify() {
        context?.let {
            if (_verificationCode.value.text.length == 6) {
                // 인증 확인 요청
                CoroutineScope(Dispatchers.IO).launch {
                    val response = repository.verifyAuthCode(
                        phoneNumber.value.text,
                        _name.value.text,
                        _verificationCode.value.text
                    )

                    if (response.success) {
                        _isVerificationCodeCorret.value = true

                        // 사용자 정보 저장
                        response.data?.let { data ->
                            editor?.putInt("userId", data.user.id.toInt()) // 사용자 ID 저장
                            editor?.putString("username", data.user.username) // 사용자 이름 저장
                            editor?.putString("name", data.user.name) // 이름 저장
                            editor?.putString("jwtAccessToken", data.jwtToken.accessToken) // 액세스 토큰 저장
                            editor?.putString("jwtRefreshToken", data.jwtToken.refreshToken) // 리프레시 토큰 저장
                            editor?.apply() // 변경사항 저장
                        }

                        // 성공 시 다음 단계로 진행
                        onSubmit()
                    } else {
                        _isVerificationCodeCorret.value = false
                        Toast.makeText(it, response.message, Toast.LENGTH_SHORT).show()
                        triggerVibration(it)
                    }
                }
            } else {
                Toast.makeText(it, "인증번호는 6자리 입니다.", Toast.LENGTH_SHORT).show()
                triggerVibration(it)
                _verificationCode.value = TextFieldValue() // 입력 필드 비움
            }
        }
    }

    private fun triggerVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }

    fun onSubmit() {
        // 유저 정보 저장 및 화면 전환
        isSignUp = true
        _screenNavigationTo.value = ScreenNavigationTo(ScreenType.PASSWORDSETTING)

        editor?.putString("name", _name.value.text)
        // 서버로부터 응답이 있을 때는 사용자 잔여금을 받아와서 저장
        editor?.putInt("balance", 8000) // 예시로 8000으로 설정
        editor?.apply()
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

    /////////////////////////// PasswordSetting ///////////////////////////

    private val _passwordCheck = MutableStateFlow(false)
    val passwordCheck: StateFlow<Boolean> = _passwordCheck

    var inputEnable: Boolean = true
    var PassWord = ""

    override fun onClickedButton(type: Int) {
        numPadClickHandler.onClickedButton(type)
    }

    val password: StateFlow<String> get() = numPadClickHandler.password

    private val numPadClickHandler: NumPadClickHandler by lazy {
        NumPadClickHandler(
            context = context!!,
            onPasswordComplete = { password: String ->
                if (!passwordCheck.value) {
                    _passwordCheck.value = true
                    PassWord = password
                    numPadClickHandler.clearPassword()
                }
                else
                {
                    inputEnable = false
                    checkPassword()
                }
            }
        )
    }

    // 입력된 비밀번호 체크
    private fun checkPassword() {
        context?.let {
            if (password.value == PassWord) {
                // 회원가입이라면 naverlogin으로 screen 전환
                // 로그인이라면 main으로 activity 전환
                if (isSignUp) _screenNavigationTo.value = ScreenNavigationTo(ScreenType.NAVERLOGIN)
                else _activityNavigationTo.value = ActivityNavigationTo(ActivityType.MAIN)

                editor?.putString("password", password.value)
                editor?.apply()

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
                numPadClickHandler.clearPassword()
            }
        }
    }

    /////////////////////////// NaverLogin ///////////////////////////

    private val _naverID = MutableStateFlow(TextFieldValue())
    val naverID: StateFlow<TextFieldValue> = _naverID

    private val _naverPassword = MutableStateFlow(TextFieldValue())
    val naverPassword: StateFlow<TextFieldValue> = _naverPassword

    val userName: String?
        get() = sharedPreferences?.getString("name", "")

    fun onNaverIDChanged(newValue: TextFieldValue) {
        _naverID.value = newValue
    }

    fun onNaverPasswordChanged(newValue: TextFieldValue) {
        _naverPassword.value = newValue
    }

    fun onLogin() {
        // 로그인 처리 로직
        context?.let {
            if (_naverID.value.text == "naver" && _naverPassword.value.text == "1111") {
                Toast.makeText(it, "로그인 성공", Toast.LENGTH_SHORT).show()
                _screenNavigationTo.value = ScreenNavigationTo(ScreenType.SIGNUPCOMPLETE)
            } else {
                Toast.makeText(it, "로그인 실패", Toast.LENGTH_SHORT).show()
                _naverID.value = TextFieldValue()
                _naverPassword.value = TextFieldValue()
            }
        }
    }

    /////////////////////////// SignUpComplete ///////////////////////////

    fun onClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.MAIN)
    }

}
