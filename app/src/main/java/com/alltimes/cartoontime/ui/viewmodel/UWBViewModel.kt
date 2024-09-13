package com.alltimes.cartoontime.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.UIStateModel
import com.alltimes.cartoontime.data.model.UwbAddressModel // UwbAddressModel을 가져옵니다.
import com.alltimes.cartoontime.data.network.uwb.UwbControllerCommunicator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UWBViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    private val uwbCommunicator: UwbControllerCommunicator = UwbControllerCommunicator(context)

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    fun connectToDevice(address: String) {
        viewModelScope.launch {
            // 주소 문자열을 ByteArray로 변환합니다.
            val addressByteArray = address.split(":").map { it.toInt(16).toByte() }.toByteArray()
            val addressModel = UwbAddressModel(addressByteArray)
            uwbCommunicator.startCommunication(addressModel.getAddressAsString())
            //uwbCommunicator.UwbConnection(addressModel)
        }
    }
}
