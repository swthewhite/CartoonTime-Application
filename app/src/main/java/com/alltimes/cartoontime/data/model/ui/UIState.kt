package com.alltimes.cartoontime.data.model.ui

import android.bluetooth.BluetoothDevice

// UI 상태를 나타내는 데이터 클래스
data class UIStateModel(
    // 화면 작동 상태 ( 시작, 정지 => 이 값으로 버튼에 써지는 이름을 바꾸면 됨 )
    val isRunning: Boolean = false,
    val isLogin: Boolean = false
)

// UI 상태를 저장하는 데이터 클래스
data class SendUiState(
    val isSending: Boolean = false,
    val isScanning: Boolean = false,
    val foundDevices: List<BluetoothDevice> = emptyList(),
    val activeDevice: BluetoothDevice? = null,
    val isDeviceConnected: Boolean = false,
    val discoveredCharacteristics: Map<String, List<String>> = emptyMap(),
    val dataBLERead: String? = null,
    val successfulDataWrites: Int = 0,
)
