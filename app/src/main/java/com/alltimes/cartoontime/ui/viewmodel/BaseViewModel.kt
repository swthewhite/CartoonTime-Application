package com.alltimes.cartoontime.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.network.utils.NetworkStatusTracker
import com.alltimes.cartoontime.data.remote.ApiService
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    private val networkStatusTracker = NetworkStatusTracker(application)

    // 네트워크 상태를 관리하는 StateFlow
    private val _networkStatus = MutableStateFlow(networkStatusTracker.networkStatus.value)
    val networkStatus = _networkStatus.asStateFlow()

    // 서버 통신 관련 repository 변수 선언
    protected val repository: UserRepository = UserRepository(
        RetrofitClient.getRetrofitInstance(context).create(ApiService::class.java)
    )

    // Activity navigation을 위한 LiveData
    protected val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    // Screen navigation을 위한 LiveData
    protected val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    // SharedPreferences 접근
    protected val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Editor 객체를 가져옵니다.
    protected val editor = sharedPreferences.edit()

    init {
        viewModelScope.launch {
            networkStatusTracker.networkStatus.collect { status ->
                _networkStatus.value = !status
            }
        }
    }

    // Activity로 이동
    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    // Screen으로 이동
    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }
}