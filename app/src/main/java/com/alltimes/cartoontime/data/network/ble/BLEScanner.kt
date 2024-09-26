package com.alltimes.cartoontime.data.network.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.alltimes.cartoontime.data.model.BLEConstants.UWB_KIOSK_SERVICE_UUID
import com.alltimes.cartoontime.data.model.BLEConstants.UWB_WITCH_SERVICE_UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import com.alltimes.cartoontime.data.model.Permissions

/**
 * BluetoothManager를 이용하여 BluetoothAdapter와 BluetoothLeScanner를 초기화
 */
class BLEScanner(context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager
        ?: throw Exception("이 디바이스는 Bluetooth 지원하지 않습니다")
    private val bluetoothAdapter = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner
        get() = bluetoothAdapter.bluetoothLeScanner

    val isScanning = MutableStateFlow(false)
    val foundDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())

    /**
     * Process a single scan result
     * 이름이 없으면 null 반환해 처리 중단하는 함수
     */
    @SuppressLint("MissingPermission")
    private fun processScanResult(result: ScanResult?): BluetoothDevice? {
        result ?: return null

        val deviceName = result.device.name ?: return null

        if (deviceName.isEmpty()) return null

        Log.i("BLEScanner", "Device found: $deviceName")

        return result.device
    }

    /**
     * Callback for BLE scan results
     */
    @RequiresPermission(Permissions.BLUETOOTH_SCAN)
    private val scanCallback = object : ScanCallback() {
        /**
         * Process a single scan result
         */
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            processScanResult(result)?.let { device ->
                if (foundDevices.value.none { it.address == device.address }) {
                    foundDevices.update { it + device }
                }
            }
        }
        /**
         * Process a batch of scan results
         */
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results ?: return

            val newDevices = results.mapNotNull { processScanResult(it) }
                .filterNot{foundDevices.value.any{info->info.address == it.address}}

            if (newDevices.isNotEmpty()) {
                foundDevices.update{it+newDevices}
            }
        }

        /**
         * Handle scan failure
         */
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning.value = false
        }
    }

    /**
     * Start scanning for BLE devices
     */
    @RequiresPermission(Permissions.BLUETOOTH_SCAN)
    fun start(mode: String) {
        Log.i("BLEScanner", "Start scanning")

        val filters = when (mode) {
            "KIOSK" -> {
                listOf(
                    ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid(UWB_KIOSK_SERVICE_UUID))
                        .build()
                )
            }
            "WITCH" -> {
                listOf(
                    ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid(UWB_WITCH_SERVICE_UUID))
                        .build()
                )
            }

            else -> {
                listOf(
                    ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid(UWB_KIOSK_SERVICE_UUID))
                        .build(),
                    ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid(UWB_WITCH_SERVICE_UUID))
                        .build()
                )
            }
        }

        // Set ScanSettings
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        //scanner.startScan(filters, settings, scanCallback)
        scanner.startScan(scanCallback)
        isScanning.value = true
    }

    /**
     * Stop scanning for BLE devices
     */
    @RequiresPermission(Permissions.BLUETOOTH_SCAN)
    fun stop() {
        Log.i("BLEScanner", "Stop scanning")
        scanner.stopScan(scanCallback)
        isScanning.value = false
    }
}