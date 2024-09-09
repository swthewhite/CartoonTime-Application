package com.alltimes.cartoontime.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.data.model.NavigationTo

class MainViewModel : ViewModel() {

    private val _navigationTo = MutableLiveData<NavigationTo>()
    val navigationTo: LiveData<NavigationTo> get() = _navigationTo

    fun onSendButtonClick() {
        _navigationTo.value = NavigationTo(ActivityType.SEND)
    }

    fun onReceiveButtonClick() {
        _navigationTo.value = NavigationTo(ActivityType.RECEIVE)
    }
}