package com.alltimes.cartoontime.data.network

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import com.alltimes.cartoontime.data.model.Permissions
import com.alltimes.cartoontime.data.model.BLEConstants

@Suppress("DEPRECATION")
class BLEDeviceConnection @RequiresPermission("PERMISSION_BLUETOOTH_CONNECT") constructor(
    // Context와 BluetoothDevice를 받아서 초기화
    private val context: Context,
    private val bluetoothDevice: BluetoothDevice
) {
    // 연결 상태를 저장하는 MutableStateFlow
    val isConnected = MutableStateFlow(false)
    // 읽은 데이터를 저장하는 MutableStateFlow
    val dataBLERead = MutableStateFlow<String?>(null)
    // 성공적으로 데이터를 쓴 횟수를 저장하는 MutableStateFlow
    val successfulDataWrites = MutableStateFlow(0)
    // GATT 서비스 목록을 저장하는 MutableStateFlow
    val services = MutableStateFlow<List<BluetoothGattService>>(emptyList())
    // BluetoothGatt 객체를 저장하는 변수
    private var gatt: BluetoothGatt? = null

    // BluetoothGattCallback을 상속받은 callback 객체
    private val callback = object: BluetoothGattCallback() {
        // 연결 상태 변경 처리 함수
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            // 연결 상태 변경 처리 함수 호출
            handleConnectionStateChange(gatt, newState)
        }

        // GATT 서비스 목록을 업데이트하는 함수
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            // GATT 서비스 목록을 업데이트
            services.value = gatt.services
        }

        // 읽은 데이터를 처리하는 함수
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            // 읽은 데이터가 CONTROLEE_CHARACTERISTIC_UUID인 경우 dataBLERead에 저장
            handleCharacteristicRead(characteristic)
        }

        // 쓴 데이터를 처리하는 함수
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            // 쓴 데이터가 CONTROLLER_CHARACTERISTIC_UUID인 경우 successfulDataWrites를 업데이트
            handleCharacteristicWrite(characteristic)
        }
    }

    // 연결 상태 변경 처리 함수
    private fun handleConnectionStateChange(gatt: BluetoothGatt, newState: Int) {
        // 연결 상태가 변경되면 연결 상태를 저장
        val connected = newState == BluetoothGatt.STATE_CONNECTED
        isConnected.value = connected
        // 연결되면 GATT 서비스 목록을 업데이트
        if (connected) services.value = gatt.services
    }

    // 특성 읽기 처리 함수
    private fun handleCharacteristicRead(characteristic: BluetoothGattCharacteristic) {
        if (characteristic.uuid == BLEConstants.CONTROLEE_CHARACTERISTIC_UUID) {
            dataBLERead.value = String(characteristic.value)
        }
    }

    // 특성 쓰기 처리 함수
    private fun handleCharacteristicWrite(characteristic: BluetoothGattCharacteristic) {
        if (characteristic.uuid == BLEConstants.CONTROLLER_CHARACTERISTIC_UUID) {
            successfulDataWrites.update { it + 1 }
        }
    }

    // GATT 연결을 위한 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun connect() {
        gatt = bluetoothDevice.connectGatt(context, false, callback)
    }

    // GATT 연결 해제를 위한 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    // GATT 서비스 목록을 찾는 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun discoverServices() {
        gatt?.discoverServices()
    }

    // 특성 읽기 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun readCharacteristic() {
        val service = gatt?.getService(BLEConstants.UWB_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(BLEConstants.CONTROLEE_CHARACTERISTIC_UUID)
        if (characteristic != null) {
            val success = gatt?.readCharacteristic(characteristic)
            Log.v("bluetooth", "Read status: $success")
        }
    }

    //  특성 쓰기 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun writeCharacteristic() {
        val service = gatt?.getService(BLEConstants.UWB_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(BLEConstants.CONTROLLER_CHARACTERISTIC_UUID)
        if (characteristic != null) {
            characteristic.value = "CONTROLLER".toByteArray()
            val success = gatt?.writeCharacteristic(characteristic)
            Log.v("bluetooth", "Write status: $success")
        }
    }
}