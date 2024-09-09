package com.alltimes.cartoontime.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.network.ble.BLEDeviceConnection
import com.alltimes.cartoontime.data.network.ble.BLEScanner
import com.alltimes.cartoontime.data.network.ble.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

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

class SendViewModel(private val application: Application) : AndroidViewModel(application) {


    private val bleScanner = BLEScanner(application)

    // Now activeConnection and dataBLERead are MutableStateFlow
    private val _activeConnection = MutableStateFlow<BLEDeviceConnection?>(null)
    private val dataBLERead = MutableStateFlow<String?>(null)

    private val triedDevices = mutableSetOf<String>()

    // StateFlow for UI state
    private val _uiState = MutableStateFlow(SendUiState())
    val uiState = combine(
        _uiState,
        _activeConnection,
        dataBLERead,
    ) { state, connection, dataRead ->
        state.copy(
            isDeviceConnected = connection != null && connection.isConnected.value,
            dataBLERead = dataRead
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SendUiState())


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
            Log.d("SendViewModel", "Scanning started")
            _uiState.update { it.copy(isScanning = true) }

            CoroutineScope(Dispatchers.Main).launch {
                bleScanner.startScanning()

                bleScanner.foundDevices.collectLatest { devices ->
                    for (deviceInfo in devices) {
                        if (!triedDevices.contains(deviceInfo.device.address)) {
                            connectToDeviceSequentially(deviceInfo)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectToDeviceSequentially(deviceInfo: DeviceInfo) {
        // Add device to the tried list
        triedDevices.add(deviceInfo.device.address)

        _activeConnection.value = BLEDeviceConnection(application, deviceInfo)
        val activeConnection: BLEDeviceConnection? = _activeConnection.value

        withContext(Dispatchers.Main) {
            activeConnection?.connect()

            // Ensure we wait until the device is connected
            activeConnection?.isConnected?.collectLatest { isConnected ->
                if (isConnected) {
                    Log.w("BLEConnection", "Connected to device: ${deviceInfo.device.name}")

                    // Wait for service discovery to complete
                    CoroutineScope(Dispatchers.Main).launch {
                        activeConnection?.discoverServices()
                        activeConnection?.serviceDiscoveryCompleted?.collectLatest { servicesDiscovered ->
                            if (servicesDiscovered) {
                                Log.d("BLEConnection", "Services discovered successfully.")

                                // Wait for password reading to complete
                                activeConnection?.readCharacteristic()
                                activeConnection?.passwordReadCompleted?.collectLatest { dataBLERead ->
                                    if (dataBLERead) {
                                        Log.d("BLEConnection", "Password read successfully.")

                                        // Wait for name writing to complete
                                        activeConnection?.writeCharacteristic()
                                        activeConnection?.nameWrittenCompleted?.collectLatest { nameWritten ->
                                            if (nameWritten) {
                                                Log.d("BLEConnection", "Name written successfully.")
                                                _uiState.update {
                                                    it.copy(
                                                        isDeviceConnected = true,
                                                        activeDevice = deviceInfo.device
                                                    )
                                                }
                                                Log.d("BLEConnection", "$dataBLERead $nameWritten")
                                            } else {
                                                Log.e("BLEConnection", "Failed to write name.")
                                            }
                                        }
                                    } else {
                                        Log.e("BLEConnection", "Failed to read password.")
                                    }
                                }
                            } else {
                                Log.e("BLEConnection", "Failed to discover services.")
                            }
                        }
                    }
                } else {
                    Log.d("BLEConnection", "Failed to connect to device: ${deviceInfo.device.name}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnectDevice() {
        Log.d("SendViewModel", "Disconnecting device")
        _activeConnection.value?.disconnect()
        _uiState.update { it.copy(isDeviceConnected = false, activeDevice = null) }

        // Clear triedDevices when disconnecting
        triedDevices.clear()

        // Ensure scanning is stopped if needed
        if (_uiState.value.isScanning) {
            bleScanner.stopScanning()
            Log.d("SendViewModel", "Scanning stopped after disconnecting")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCleared() {
        super.onCleared()

        if (bleScanner.isScanning.value) {
            Log.d("SendViewModel", "Stopping scanning on ViewModel cleared")
            bleScanner.stopScanning()
        }
    }
}
