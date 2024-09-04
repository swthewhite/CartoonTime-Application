package com.alltimes.cartoontime.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ButtonAction
import com.alltimes.cartoontime.data.model.ActionType

class MainViewModel : ViewModel() {

    private val _action = MutableLiveData<ButtonAction>()
    val action: LiveData<ButtonAction> get() = _action

    fun onSendButtonClick() {
        _action.value = ButtonAction(ActionType.SEND)
    }

    fun onReceiveButtonClick() {
        _action.value = ButtonAction(ActionType.RECEIVE)
    }
}