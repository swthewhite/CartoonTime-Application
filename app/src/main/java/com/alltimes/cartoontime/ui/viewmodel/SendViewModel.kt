package com.alltimes.cartoontime.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.network.BLEDeviceConnection
import com.alltimes.cartoontime.data.network.BLEScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

// UI 상태를 저장하는 데이터 클래스
data class SendUiState(
    val isSending: Boolean = false,
    val isScanning: Boolean = false,
    val foundDevices: List<BluetoothDevice> = emptyList(),
    val activeDevice: BluetoothDevice? = null,
    val isDeviceConnected: Boolean = false,
    val discoveredCharacteristics: Map<String, List<String>> = emptyMap(),
    val dataBLERead: String? = null,
    val successfulDataWrites: Int = 0
)

class SendViewModel(application: Application) : AndroidViewModel(application) {

    private val bleScanner = BLEScanner(application)
    private var activeConnection = MutableStateFlow<BLEDeviceConnection?>(null)

    // 상태를 저장하는 StateFlow
    private val _uiState = MutableStateFlow(SendUiState())
    val uiState: StateFlow<SendUiState> = _uiState

    // 버튼 클릭 시 호출되는 함수
    fun onSendButtonClick() {
        _uiState.value = _uiState.value.copy(isSending = !_uiState.value.isSending)

        if (_uiState.value.isSending) {
            startScanningAndConnect()
        } else {
            disconnectDevice()
        }
    }

    // 스캔 시작, 장치 선택, 연결, 서비스 검색, 비밀번호 읽기, 이름 쓰기를 모두 처리하는 함수
    @SuppressLint("MissingPermission")
    fun startScanningAndConnect() {
        viewModelScope.launch {
            Log.d("SendViewModel", "startScanningAndConnect")
            bleScanner.startScanning()
            _uiState.update { it.copy(isScanning = true) }

            bleScanner.foundDevices.collect { devicesInfo ->
                if (devicesInfo.isNotEmpty()) {
                    val deviceInfo = devicesInfo.firstOrNull()
                    if (deviceInfo != null) {
                        setActiveDevice(deviceInfo.device)
                        connectToDeviceAndPerformActions()
                    }
                    bleScanner.stopScanning()
                    _uiState.update { it.copy(isScanning = false) }
                }
            }
        }
    }

    private fun setActiveDevice(device: BluetoothDevice) {
        activeConnection.value = BLEDeviceConnection(getApplication(), device)
        _uiState.update { it.copy(activeDevice = device) }
    }

    private fun connectToDeviceAndPerformActions() {
        activeConnection.value?.let { connection ->
            connection.connect()

            viewModelScope.launch {
                connection.discoverServices()
                connection.readCharacteristic()
                connection.writeCharacteristic()

                _uiState.update { uiState ->
                    uiState.copy(
                        isDeviceConnected = true,
                        discoveredCharacteristics = connection.services.value
                            .associate { it.uuid.toString() to it.characteristics.map { it.uuid.toString() } },
                        dataBLERead = connection.dataBLERead.value,
                        successfulDataWrites = connection.successfulDataWrites.value
                    )
                }
            }
        }
    }

    private fun disconnectDevice() {
        activeConnection.value?.disconnect()
        _uiState.update { it.copy(isDeviceConnected = false, activeDevice = null) }
    }

    override fun onCleared() {
        super.onCleared()

        if (bleScanner.isScanning.value) {
            bleScanner.stopScanning()
        }
    }
}
