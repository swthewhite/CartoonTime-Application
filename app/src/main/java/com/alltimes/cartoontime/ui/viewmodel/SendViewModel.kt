package com.alltimes.cartoontime.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.common.NumpadAction
import com.alltimes.cartoontime.common.PointpadAction
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.network.ble.BLEDeviceConnection
import com.alltimes.cartoontime.data.network.ble.BLEScanner
import com.alltimes.cartoontime.data.network.ble.DeviceInfo
import com.alltimes.cartoontime.ui.handler.NumPadClickHandler
import com.alltimes.cartoontime.ui.handler.PointPadClickHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

class SendViewModel(private val context: Context) : ViewModel(), NumpadAction, PointpadAction {
//class SendViewModel(private val application: Application) : AndroidViewModel(application) {

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

    /////////////////////////// PointInput ///////////////////////////

    override fun onPointClickedButton(type: Int) {
        pointPadClickHandler.onClickedButton(type, balance.value)
    }

    val point: StateFlow<String> get() = pointPadClickHandler.point

    // 눌리는 버튼에 대한 구현
    private val pointPadClickHandler: PointPadClickHandler by lazy {
        PointPadClickHandler(
            context = context,
            isPointExceeded = {
                if (point.value.toIntOrNull() ?: 0 > balance.value) {
                    showPointError()
                    pointPadClickHandler.setPoint(balance.toString())
                }
            }
        )
    }

    private fun showPointError() {
        Toast.makeText(context, "포인트가 초과되었습니다", Toast.LENGTH_SHORT).show()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }


    /////////////////////////// PasswordInput ///////////////////////////

    override fun onClickedButton(type: Int) {
        numPadClickHandler.onClickedButton(type)
    }

    val password: StateFlow<String> get() = numPadClickHandler.password

    private val numPadClickHandler: NumPadClickHandler by lazy {
        NumPadClickHandler(
            context = context,
            onPasswordComplete = { password: String ->
                val userPassword = sharedPreferences.getString("password", null)
                if (userPassword == password) {
                    goScreen(ScreenType.SENDDESCRIPTION)
                } else {
                    numPadClickHandler.clearPassword()
                    showPasswordError()
                }
            }
        )
    }

    private fun showPasswordError() {
        Toast.makeText(context, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }


    /////////////////////////// Description ///////////////////////////

    // BLE 연결

    /////////////////////////// Loading ///////////////////////////

    // UWB 연결
    // 포인트 송금 함수

    /////////////////////////// Confirm ///////////////////////////


    private val bleScanner = BLEScanner(context)

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

        _activeConnection.value = BLEDeviceConnection(context, deviceInfo)
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
