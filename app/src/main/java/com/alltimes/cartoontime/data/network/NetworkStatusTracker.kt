package com.alltimes.cartoontime.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkStatusTracker(context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // 네트워크 상태를 관리하는 StateFlow
    private val _networkStatus = MutableStateFlow<Boolean>(false) // 초기 상태는 false로 설정
    val networkStatus: StateFlow<Boolean> get() = _networkStatus

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkStatus.value = true // 네트워크가 연결되었을 때
            }

            override fun onLost(network: Network) {
                _networkStatus.value = false // 네트워크가 끊겼을 때
            }
        })
    }
}