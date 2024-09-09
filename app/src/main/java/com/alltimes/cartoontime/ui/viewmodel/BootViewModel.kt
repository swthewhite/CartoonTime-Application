package com.alltimes.cartoontime.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.data.model.NavigationTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BootViewModel : ViewModel() {

    private val _navigationTo = MutableLiveData<NavigationTo>()
    val navigationTo: LiveData<NavigationTo> get() = _navigationTo

    fun onLoginClick() {
        // 로그인 처리 로직
        _navigationTo.value = NavigationTo(ActivityType.MAIN)
    }

    fun onSignUpClick() {
        // 회원가입 처리 로직
        _navigationTo.value = NavigationTo(ActivityType.SIGNUP)
    }

    sealed class NavigationEvent {
        object Login : NavigationEvent()
        object SignUp : NavigationEvent()
    }
}