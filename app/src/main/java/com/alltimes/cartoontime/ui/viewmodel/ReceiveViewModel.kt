package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.UIStateModel
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiveViewModel(private val context: Context) : ViewModel() {
//class ReceiverViewModel(application: Application) : AndroidViewModel(application) {

    /////////////////////////// 공용 ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    // SharedPreferences 객체를 가져옵니다.
    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Editor 객체를 가져옵니다.
    val editor = sharedPreferences.edit()

    var inputEnable: Boolean = true

    private val _balance = MutableStateFlow(sharedPreferences.getInt("balance", 0))
    val balance: StateFlow<Int> = _balance

    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }

    /////////////////////////// Description ///////////////////////////


    /////////////////////////// Loading ///////////////////////////


    /////////////////////////// Confirm ///////////////////////////


    private val bleViewModel: BLEViewModel = BLEViewModel(context)
    private val uwbViewModel: UWBViewModel = UWBViewModel(context)

    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    fun onButtonClick() {
        _uiState.update { it.copy(isRunning = !it.isRunning) }
        bleViewModel.onButtonClick()
    }

    fun connectToDevice(address: String) {
        uwbViewModel.connectToDevice(address)
    }
}
