package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ActivityType

class MainViewModel(private val context: Context) : ViewModel() {


    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    fun onSendButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.SEND)
    }

    fun onReceiveButtonClick() {
        _activityNavigationTo.value = ActivityNavigationTo(ActivityType.RECEIVE)
    }
}