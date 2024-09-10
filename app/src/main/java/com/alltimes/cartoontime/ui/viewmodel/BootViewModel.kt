package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ActivityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BootViewModel(private val context: Context) : ViewModel() {

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    fun onLoginClick() {
        // 로그인 처리 로직
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.MAIN)
    }

    fun onSignUpClick() {
        // 회원가입 처리 로직
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SIGNUP)
    }

    sealed class NavigationEvent {
        object Login : NavigationEvent()
        object SignUp : NavigationEvent()
    }
}