package com.alltimes.cartoontime.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.data.model.SendUiState
import com.alltimes.cartoontime.data.model.UIStateModel
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.ble.BLEDeviceConnection
import com.alltimes.cartoontime.data.network.ble.BLEScanner
import com.alltimes.cartoontime.data.network.ble.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BLEScannerViewModel(private val context: Context) : ViewModel(), RangingCallback {

    // mode: true - login
    // mode: false - money transaction
    private val _mode = MutableStateFlow(false)
    val mode = _mode.asStateFlow()

    private val bleScanner = BLEScanner(context)

    // Now activeConnection and dataBLERead are MutableStateFlow
    private val _activeConnection = MutableStateFlow<BLEDeviceConnection?>(null)
    private val dataBLERead = MutableStateFlow<String?>(null)

    private val triedDevices = mutableSetOf<String>()

    // StateFlow for UI state
    private val _sendUiState = MutableStateFlow(SendUiState())
    val sendUiState = combine(
        _sendUiState,
        _activeConnection,
        dataBLERead,
    ) { state, connection, dataRead ->
        state.copy(
            isDeviceConnected = connection != null && connection.isConnected.value,
            dataBLERead = dataRead
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SendUiState())

    fun setMode(value: Boolean) {
        _mode.update { value }
    }

    // 버튼 클릭 시 호출되는 함수
    fun onSendButtonClick() {
        _sendUiState.value = _sendUiState.value.copy(isSending = !_sendUiState.value.isSending)

        if (_sendUiState.value.isSending) {
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
            _sendUiState.update { it.copy(isScanning = true) }

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

        _activeConnection.value = BLEDeviceConnection(context, deviceInfo, this)
        val activeConnection: BLEDeviceConnection? = _activeConnection.value
        // mode setting
        activeConnection?.setMode(_mode.value)

        withContext(Dispatchers.Main) {
            activeConnection?.connect()

            // Ensure we wait until the device is connected
            activeConnection?.isConnected?.collectLatest { isConnected ->
                if (isConnected) {
                    //Log.w("BLEConnection", "Connected to device: ${deviceInfo.device.name}")

                    // Wait for service discovery to complete
                    CoroutineScope(Dispatchers.Main).launch {
                        activeConnection?.discoverServices()
                        activeConnection?.serviceDiscoveryCompleted?.collectLatest { servicesDiscovered ->
                            if (servicesDiscovered) {
                                //Log.d("BLEConnection", "Services discovered successfully.")

                                // Wait for password reading to complete
                                activeConnection?.readCharacteristic()
                                activeConnection?.passwordReadCompleted?.collectLatest { dataBLERead ->
                                    if (dataBLERead) {
                                        //Log.d("BLEConnection", "Password read successfully.")

                                        // Wait for name writing to complete
                                        activeConnection?.writeCharacteristic()
                                        activeConnection?.nameWrittenCompleted?.collectLatest { nameWritten ->
                                            if (nameWritten) {
                                                //Log.d("BLEConnection", "Name written successfully.")
                                                _sendUiState.update {
                                                    it.copy(
                                                        isDeviceConnected = true,
                                                        activeDevice = deviceInfo.device
                                                    )
                                                }
                                                //Log.d("BLEConnection Data", "$dataBLERead $nameWritten")
                                            } else {
                                                //Log.e("BLEConnection", "Failed to write name.")
                                            }
                                        }
                                    } else {
                                        //Log.e("BLEConnection", "Failed to read password.")
                                    }
                                }
                            } else {
                                //Log.e("BLEConnection", "Failed to discover services.")
                            }
                        }
                    }
                } else {
                    //Log.d("BLEConnection", "Failed to connect to device: ${deviceInfo.device.name}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice() {
        Log.d("SendViewModel", "Disconnecting device")
        _activeConnection.value?.disconnect()
        _sendUiState.update { it.copy(isDeviceConnected = false, activeDevice = null) }

        // Clear triedDevices when disconnecting
        triedDevices.clear()

        // Ensure scanning is stopped if needed
        if (_sendUiState.value.isScanning) {
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

    fun setUIState(value: Boolean) {
        _sendUiState.update {
            it.copy(
                isDeviceConnected = value,
            )
        }
    }


    /////////////// uwb controlee viewmodel /////////////////


    private val _uiState = MutableStateFlow(UIStateModel())
    val uiState = _uiState.asStateFlow()

    private var measurementCount = 0
    private var sessionActive = false
    private val timeoutHandler = Handler(Looper.getMainLooper())

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    fun setSession(value: Boolean) {
        sessionActive = value
    }

    override fun onDistanceMeasured(distance: Float) {

        println("Session Active : $sessionActive")

        if (!sessionActive) return

        println("거리 측정 : ${distance}")

        // 거리 측정 로직 처리
        if (distance < 5) {
            measurementCount++
        } else {
            measurementCount = 0  // 거리 벗어나면 카운트 초기화
        }

        if (measurementCount >= 30) {
            _activeConnection.value?.disconnectUWB()
            sessionActive = false  // 세션 종료
            completeLogin()
        }

        // 10cm 이상 거리에서 타임아웃 처리
        timeoutHandler.postDelayed({
            if (distance > 10) {
                _activeConnection.value?.disconnectUWB()
                sessionActive = false  // 타임아웃 발생 시 세션 비활성화
                // 추가 처리
            }
        }, 3000)
    }

    private fun completeLogin() {
        // UI 상태 업데이트
        viewModelScope.launch {
            _uiState.update { it.copy(isLogin = true) }
        }
        // 화면 전환 처리
        // 예: navigateToMainScreen()
        println("입실 완료 처리")
    }
}