package com.alltimes.cartoontime.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.network.NetworkStatusTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    private val networkStatusTracker = NetworkStatusTracker(application)

    // 네트워크 상태를 관리하는 StateFlow
    private val _networkStatus = MutableStateFlow(networkStatusTracker.networkStatus.value)
    val networkStatus = _networkStatus.asStateFlow()

    init {
        viewModelScope.launch {
            networkStatusTracker.networkStatus.collect { status ->
                _networkStatus.value = !status
            }
        }
    }
}