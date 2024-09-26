package com.alltimes.cartoontime.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.inputmethod.InputMethodManager
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
import com.alltimes.cartoontime.data.remote.NaverAuthRequest
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.remote.SignResponse
import com.alltimes.cartoontime.data.remote.VerifyAuthRequest
import com.alltimes.cartoontime.data.repository.UserRepository
import com.alltimes.cartoontime.ui.handler.NumPadClickHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SignUpViewModel(private val context: Context?) : ViewModel(), NumpadAction {

    /////////////////////////// 공용 ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    private val sharedPreferences: SharedPreferences?
        get() = context?.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    val editor = sharedPreferences?.edit()

    // 회원가입, 로그인 구분
    var isSignUp = false
    // 네이버 아이디, 비밀번호 유무
    var isNaverInfo = false

    // 서버 통신 관련 변수
    private val repository = UserRepository(RetrofitClient.apiService)

    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    fun goScreen(screen: ScreenType) {
        println("스크린 전환")
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }

    /////////////////////////// SignUp ///////////////////////////

    // 전화번호
    private val _phoneNumber = MutableStateFlow(TextFieldValue())
    val phoneNumber: StateFlow<TextFieldValue> = _phoneNumber

    // 인증번호
    private val _verificationCode = MutableStateFlow(TextFieldValue())
    val verificationCode: StateFlow<TextFieldValue> = _verificationCode

    // 이름 , isSignUp 이 true 일 때만 사용
    private val _name = MutableStateFlow(TextFieldValue())
    val name: StateFlow<TextFieldValue> = _name

    // 전화번호 입력 가능 여부
    private val _isPhoneNumberEnable = MutableStateFlow(true)
    val isPhoneNumberEnable: StateFlow<Boolean> = _isPhoneNumberEnable

    // 이름 입력 가능 여부
    private val _isNameEnable = MutableStateFlow(true)
    val isNameEnable: StateFlow<Boolean> = _isNameEnable

    // 인증번호 입력 창 표시 여부
    private val _isVerificationCodeVisible = MutableStateFlow(false)
    val isVerificationCodeVisible: StateFlow<Boolean> = _isVerificationCodeVisible

    // 인증번호 확인용
    private val _isVerificationCodeCorret = MutableStateFlow(false)
    val isVerificationCodeCorrect: StateFlow<Boolean> = _isVerificationCodeCorret

    // 이름 검사, 버튼 활성화용
    private val _isNameCorrect = MutableStateFlow(false)
    val isNameCorrect: StateFlow<Boolean> = _isNameCorrect

    // 버튼 활성화용 (회원가입, 로그인)
    private val _isSubmitButtonEnabled = MutableStateFlow(false)
    val isSubmitButtonEnabled: StateFlow<Boolean> = _isSubmitButtonEnabled

    // 등록하기 버튼 활성화 여부 검사
    private fun checkSubmitButtonState() {
        context?.let {
            // 이름 검사 로직
            _isNameCorrect.value = _name.value.text.length >= 2
            // 전체적인 버튼 활성화 로직 ( 인증번호가 맞고, 이름이 제대로 입력되었다면 )
            _isSubmitButtonEnabled.value = _isVerificationCodeCorret.value && _isNameCorrect.value
        }
    }

    // 인증번호 요청
    fun onRequestVerificationCode() {
        context?.let {
            // 전화번호를 받아와서
            val phoneNumberString = phoneNumber.value.text
            val isValidPhoneNumber = phoneNumberString.matches(Regex("^010\\d{8}$"))

            // 전화번호 형식이 맞다면
            if (isValidPhoneNumber) {
                _isVerificationCodeVisible.value = true
                _isPhoneNumberEnable.value = false

                // 인증 코드 요청
                CoroutineScope(Dispatchers.IO).launch {
                    val response = repository.requestAuthCode(phoneNumberString)

                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            Toast.makeText(it, response.message, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(it, response.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(it, "전화번호 형식을 지켜주세요", Toast.LENGTH_SHORT).show()

                // 전화번호 필드 초기화
                _phoneNumber.value = TextFieldValue()
            }
        }
    }

    fun onVerify() {
        context?.let {
            // 인증번호는 6자리
            if (_verificationCode.value.text.length == 6) {

                val verifyAuthRequest =
                    VerifyAuthRequest(phoneNumber.value.text, _verificationCode.value.text)

                // 인증 확인 요청
                CoroutineScope(Dispatchers.IO).launch {

                        val response = try {
                            repository.verifyAuthCode(verifyAuthRequest)
                        } catch (e: HttpException) {
                            // HTTP 예외를 잡아 UI에 표시하거나 적절히 처리
                            withContext(Dispatchers.Main) {
                                triggerVibration(it)
                                _verificationCode.value = TextFieldValue()
                                Toast.makeText(it, "인증번호가 틀립니다", Toast.LENGTH_SHORT).show()
                            }
                            return@launch // 예외 발생 시 함수 종료
                        } catch (e: Exception) {
                            // 다른 일반 예외 처리
                            withContext(Dispatchers.Main) {
                                Toast.makeText(it, "알 수 없는 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            return@launch
                        }

                        // 키패드 숨기기
                        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        val view = (context as Activity).currentFocus
                        view?.let {
                            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                        }

                        if (response.success) {
                            _isVerificationCodeCorret.value = true

                            // 유저 정보 받아오기
                            val userId = response.data?.userId

                            // 응답 정보를 보고 회원가입인지 로그인인지 구분
                            isSignUp = userId == -1L || userId == null

                            // userID가 있을 때 유저 정보를 받아와서 저장
                            if (userId != null && userId != -1L) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val userInfoResponse = try {
                                        repository.getUserInfo(userId)
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(it, "예상치 못한 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                        return@launch
                                    }

                                    if (userInfoResponse.success) {

                                        // 네이버 정보 받아오기
                                        // 없으면 네이버 정보 받는 쪽으로 넘겨야 함. ( 없으면 추천이 불가하니까 )

                                        editor?.putLong("balance", userInfoResponse.data?.currentMoney!!)
                                        editor?.putLong("userId", userInfoResponse.data?.id!!)
                                        editor?.putString("userName", userInfoResponse.data?.username!!)
                                        editor?.putString("name", userInfoResponse.data?.name!!)

                                        // 이름 입력 필드 사용 불가
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(it, "${userInfoResponse.data?.name}님 환영합니다.", Toast.LENGTH_SHORT).show()
                                        }
                                        _name.value = TextFieldValue(userInfoResponse.data?.name!!)
                                        _isNameCorrect.value = true
                                        _isNameEnable.value = false
                                        checkSubmitButtonState()
                                    }
                                }
                            }
                        }
                }
            } else {
                Toast.makeText(it, "인증번호는 6자리 입니다.", Toast.LENGTH_SHORT).show()
                triggerVibration(it)
                // 인증번호 필드 초기화
                _verificationCode.value = TextFieldValue()
            }
        }
    }

    // 휴대폰 진동
    private fun triggerVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }

    // 등록하기 버튼
    fun onSubmit() {
        if (isSignUp) {
            // sign-up api 호출
            CoroutineScope(Dispatchers.IO).launch {
                val response = try {
                    repository.signUp(phoneNumber.value.text, name.value.text)
                } catch (e: Exception) {
                    // 오류 처리
                    return@launch
                }

                handleResponse(response)
            }
        } else {
            // sign-in api 호출
            CoroutineScope(Dispatchers.IO).launch {
                val response = try {
                    repository.signIn(phoneNumber.value.text)
                } catch (e: Exception) {
                    // 오류 처리
                    return@launch
                }
                handleResponse(response)
            }
        }
    }

    // 회원 가입, 로그인 처리 로직
    private fun handleResponse(response: SignResponse?) {
        if (response?.success == true) {
            // 유저 정보 저장
            editor?.putLong("userId", response.data?.user?.id ?: -1)
            editor?.putString("username", response.data?.user?.username ?: "")
            editor?.putString("name", response.data?.user?.name ?: "")

            // jwtToken 저장
            editor?.putString("grantType", response.data?.jwtToken?.grantType)
            editor?.putString("accessToken", response.data?.jwtToken?.accessToken)
            editor?.putString("refreshToken", response.data?.jwtToken?.refreshToken)

            editor?.apply()

            // 화면 전환은 메인 스레드에서
            CoroutineScope(Dispatchers.Main).launch {
                goScreen(ScreenType.PASSWORDSETTING)
            }
        } else {
            // 실패 처리
        }
    }

    // 전화번호 필드 변화 감지
    fun onPhoneNumberChange(newValue: TextFieldValue) {
        _phoneNumber.value = newValue
        checkSubmitButtonState()
    }

    // 인증번호 필드 변화 감지
    fun onVerificationCodeChange(newValue: TextFieldValue) {
        _verificationCode.value = newValue
        checkSubmitButtonState()
    }

    // 이름 필드 변화 감지
    fun onNameChange(newValue: TextFieldValue) {
        _name.value = newValue
        checkSubmitButtonState()
        // 이름은 아무렇게나
        if (_name.value.text.length >= 2) {
            _isNameCorrect.value = true
        } else {
            _isNameCorrect.value = false
        }
    }

    /////////////////////////// PasswordSetting ///////////////////////////

    // 비밀번호는 두번 입력받아야 함
    private val _passwordCheck = MutableStateFlow(false)
    val passwordCheck: StateFlow<Boolean> = _passwordCheck

    // 첫번째 입력된 비밀번호
    var PassWord = ""

    // 비밀번호 입력 필드 활성화 여부
    var inputEnable: Boolean = true

    // 키패드 클릭 이벤트 처리
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
                    // 첫 번째 비밀번호 입력
                    PassWord = password
                    numPadClickHandler.clearPassword()
                } else {
                    // 두 번째 비밀번호 입력 후 체크 로직
                    inputEnable = false
                    checkPassword()
                }
            }
        )
    }

    // 입력된 두 개의 비밀번호 체크
    private fun checkPassword() {
        context?.let {
            if (password.value == PassWord) {
                // 회원가입이거나 로그인이라도 네이버 정보가 없으면 naverlogin으로 전환
                // 로그인이거나 네이버 정보가 있으면 signupscreen으로 전환
                
                // isSignUp => 네이버 정보가 있는지 없는지로 구분해도 될듯
                if (isSignUp) goScreen(ScreenType.NAVERLOGIN)
                else goScreen(ScreenType.SIGNUPCOMPLETE)

                // 비밀번호 저장
                editor?.putString("password", password.value)
                editor?.apply()

                Toast.makeText(it, "비밀번호를 저장했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(it, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()

                triggerVibration(it)

                // 입력 초기화
                inputEnable = true
                _passwordCheck.value = false
                PassWord = ""
                numPadClickHandler.clearPassword()
            }
        }
    }

    /////////////////////////// NaverLogin ///////////////////////////

    // 네이버 아이디 필드
    private val _naverID = MutableStateFlow(TextFieldValue())
    val naverID: StateFlow<TextFieldValue> = _naverID

    // 네이버 비밀번호 필드
    private val _naverPassword = MutableStateFlow(TextFieldValue())
    val naverPassword: StateFlow<TextFieldValue> = _naverPassword

    // 로그인 버튼 활성화 여부
    private val _naverLoginEnable = MutableStateFlow(false)
    val naverLoginEnable: StateFlow<Boolean> = _naverLoginEnable

    // 네이버 아이디 필드 변화 감지
    fun onNaverIDChanged(newValue: TextFieldValue) {
        _naverID.value = newValue
        checkNaverLoginButtonState()
    }

    // 네이버 비밀번호 필드 변화 감지
    fun onNaverPasswordChanged(newValue: TextFieldValue) {
        _naverPassword.value = newValue
        checkNaverLoginButtonState()
    }

    private fun checkNaverLoginButtonState() {
        _naverLoginEnable.value =
            _naverID.value.text.isNotEmpty() && _naverPassword.value.text.isNotEmpty()
    }

    // 네이버 로그인
    fun onNaverLogin() {
        context?.let {
            // 네이버 로그인 api 호출
            CoroutineScope(Dispatchers.IO).launch {
                val userId = sharedPreferences?.getLong("userId", -1L).toString()

                val response = try {
                    repository.naverAuth(NaverAuthRequest(userId, _naverID.value.text, _naverPassword.value.text))
                } catch (e: Exception) {
                    // 오류 처리
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        Toast.makeText(it, response.message, Toast.LENGTH_SHORT).show()
                        goScreen(ScreenType.SIGNUPCOMPLETE)
                    } else {
                        Toast.makeText(it, response.message, Toast.LENGTH_SHORT).show()
                        _naverID.value = TextFieldValue()
                        _naverPassword.value = TextFieldValue()
                    }
                }
            }
        }
    }

    /////////////////////////// SignUpComplete ///////////////////////////

}
