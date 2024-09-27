package com.alltimes.cartoontime.ui.handler

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NumPadClickHandler(
    private val context: Context,
    private val onPasswordComplete: (String) -> Unit
) {
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> get() = _password

    fun onClickedButton(type: Int) {

        if (type == -1) {
            if (_password.value.isNotEmpty()) _password.value = _password.value.dropLast(1)
        } else {
            _password.value += type.toString()
        }

        // 6자리 모두 입력 시 비밀번호 입력 완료 처리
        if (_password.value.length == 6) {
            onPasswordComplete(_password.value)
        }
    }

    fun clearPassword() {
        _password.value = ""
    }
}