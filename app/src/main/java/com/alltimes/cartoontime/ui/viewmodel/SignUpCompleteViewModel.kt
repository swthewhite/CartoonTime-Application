package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.data.model.NavigationTo

class SignUpCompleteViewModel(private val context: Context?) : ViewModel() {

    private val _navigationTo = MutableLiveData<NavigationTo>()
    val navigationTo: LiveData<NavigationTo> get() = _navigationTo

    fun onClick() {
        _navigationTo.value = NavigationTo(ActivityType.MAIN)
    }
}