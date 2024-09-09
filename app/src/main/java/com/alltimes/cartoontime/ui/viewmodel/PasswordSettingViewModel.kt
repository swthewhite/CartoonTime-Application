package com.alltimes.cartoontime.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PasswordSettingViewModel {
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _passwordCheck = MutableStateFlow("")
    val passwordCheck: StateFlow<String> = _passwordCheck

    // 눌리는 버튼에 대한 구현

    // 입력된 비밀번호 체크


}