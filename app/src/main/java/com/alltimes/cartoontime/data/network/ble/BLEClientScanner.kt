package com.alltimes.cartoontime.data.network.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.pow
import com.alltimes.cartoontime.data.model.Permissions

class BLEScanner(context: Context) {

    // BluetoothManager를 이용하여 BluetoothAdapter와 BluetoothLeScanner를 초기화
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager
        ?: throw Exception("이 디바이스는 Bluetooth 지원하지 않습니다")
    private val bluetoothAdapter = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner
        get() = bluetoothAdapter.bluetoothLeScanner

    // 스캔 중인지 여부를 저장하는 MutableStateFlow
    val isScanning = MutableStateFlow(false)

    // 찾은 디바이스 목록을 저장하는 MutableStateFlow
    val foundDevices = MutableStateFlow<List<DeviceInfo>>(emptyList())

    // 이름 없는 디바이스는 리스트에 추가하지 않는 함수
    @SuppressLint("MissingPermission")
    private fun processScanResult(result: ScanResult?): DeviceInfo? {
        result ?: return null
        val deviceName = result.device.name ?: return null
        Log.d("BLEScanner", "Device found: $deviceName")  // 디바이스 이름 디버그 로그로 출력

        if (deviceName.isEmpty()) return null

        val distance = calculateDistance(result.rssi, result.txPower)

        return DeviceInfo(result.device, distance)
    }

    @RequiresPermission(Permissions.BLUETOOTH_SCAN)
    private val scanCallback = object : ScanCallback() {
        // 스캔 결과를 처리하는 함수
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            processScanResult(result)?.let { deviceInfo ->
                if (foundDevices.value.none { it.device.address == deviceInfo.device.address }) {
                    foundDevices.update { it + deviceInfo }
                }
            }
        }

        // 여러 개의 스캔 결과를 처리하는 함수
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results ?: return

            val newDevices = results.mapNotNull { processScanResult(it) }
                .filterNot { foundDevices.value.any { info -> info.device.address == it.device.address } }

            if (newDevices.isNotEmpty()) {
                foundDevices.update { it + newDevices }
            }
        }

        // 스캔이 실패했을 때 호출되는 함수
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning.value = false
        }
    }

    // 스캔을 시작하는 함수, ScanCallback을 이용하여 스캔 결과를 처리
    @RequiresPermission(Permissions.BLUETOOTH_SCAN)
    fun startScanning() {
        Log.d("BLEScanner", "startScanning")
        scanner.startScan(scanCallback)
        isScanning.value = true
    }

    // 스캔을 중지하는 함수
    @RequiresPermission(Permissions.BLUETOOTH_SCAN)
    fun stopScanning() {
        scanner.stopScan(scanCallback)
        isScanning.value = false
    }

    // 대략적인 거리를 계산하는 함수, RSSI와 txPower를 이용하여 계산
    private fun calculateDistance(rssi: Int, txPower: Int): Double {
        // txPower 값이 없는 경우,
        // RSSI만으로는 정확한 계산이 불가능하여,
        // 일단 기본 거리를 반환
        if (txPower == Integer.MIN_VALUE) return -1.0

        val ratio = rssi * 1.0 / txPower
        return if (ratio < 1.0) {
            ratio.pow(10.0)
        } else {
            (0.89976 * ratio.pow(7.7095) + 0.111).pow(10.0)
        }
    }
}

// 디바이스 정보를 저장하는 데이터 클래스
data class DeviceInfo(
    val device: BluetoothDevice,
    val distance: Double
)