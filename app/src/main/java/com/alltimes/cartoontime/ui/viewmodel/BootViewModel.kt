package com.alltimes.cartoontime.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BootViewModel : ViewModel() {

    private val _navigateTo = MutableStateFlow<NavigationEvent?>(null)
    val navigateTo: StateFlow<NavigationEvent?> = _navigateTo

    fun onLoginClick() {
        // 로그인 처리 로직
        _navigateTo.value = NavigationEvent.Login
    }

    fun onSignUpClick() {
        // 회원가입 처리 로직
        _navigateTo.value = NavigationEvent.SignUp
    }

    sealed class NavigationEvent {
        object Login : NavigationEvent()
        object SignUp : NavigationEvent()
    }
}