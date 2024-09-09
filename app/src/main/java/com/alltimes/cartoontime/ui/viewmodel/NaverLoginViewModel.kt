package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.data.model.NavigationTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NaverLoginViewModel(private val context: Context) : ViewModel() {

    private val _navigationTo = MutableLiveData<NavigationTo>()
    val navigationTo: LiveData<NavigationTo> get() = _navigationTo

    private val _naverID = MutableStateFlow(TextFieldValue())
    val naverID: StateFlow<TextFieldValue> = _naverID

    private val _naverPassword = MutableStateFlow(TextFieldValue())
    val naverPassword: StateFlow<TextFieldValue> = _naverPassword

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
                _navigationTo.value = NavigationTo(ActivityType.SIGNUPCOMPLETE)
            } else {
                Toast.makeText(it, "로그인 실패", Toast.LENGTH_SHORT).show()
                _naverID.value = TextFieldValue()
                _naverPassword.value = TextFieldValue()
            }
        }
    }

}