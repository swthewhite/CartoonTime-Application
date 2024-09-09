package com.alltimes.cartoontime.data.network.ble

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
import com.alltimes.cartoontime.data.network.uwb.UwbControleeCommunicator

val CTF_SERVICE_UUID: UUID = UUID.fromString("8c380000-10bd-4fdb-ba21-1922d6cf860d")
val PASSWORD_CHARACTERISTIC_UUID: UUID = UUID.fromString("8c380001-10bd-4fdb-ba21-1922d6cf860d")
val NAME_CHARACTERISTIC_UUID: UUID = UUID.fromString("8c380002-10bd-4fdb-ba21-1922d6cf860d")

@Suppress("DEPRECATION")
class BLEDeviceConnection @RequiresPermission("PERMISSION_BLUETOOTH_CONNECT") constructor(
    // Context와 BluetoothDevice를 받아서 초기화
    private val context: Context,
    private val bluetoothDevice: DeviceInfo
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
    private val uwbCommunicator =
        UwbControleeCommunicator(
            context
        )

    // 서비스 발견 완료 여부를 저장하는 MutableStateFlow
    val serviceDiscoveryCompleted = MutableStateFlow(false)
    val passwordReadCompleted = MutableStateFlow(false)
    val nameWrittenCompleted = MutableStateFlow(false)

    // BluetoothGattCallback을 상속받은 callback 객체
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    private val callback = object: BluetoothGattCallback() {
        // 연결 상태 변경 처리 함수
        @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            // 연결 상태 변경 처리 함수 호출
            handleConnectionStateChange(gatt, newState)
        }

        // GATT 서비스 목록을 업데이트하는 함수
        @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                services.value = gatt.services
                serviceDiscoveryCompleted.value = true
                Log.d("BLE", "Services discovered: ${gatt.services.size} services found.")
                for (service in gatt.services) {
                    Log.d("BLE", "Service UUID: ${service.uuid}")
                }
            } else {
                Log.e("BLE", "Service discovery failed with status: $status")
                serviceDiscoveryCompleted.value = false
            }
        }

        // 읽은 데이터를 처리하는 함수
        @Deprecated("Deprecated in Java")
        @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            // 읽은 데이터가 CONTROLEE_CHARACTERISTIC_UUID인 경우 dataBLERead에 저장
            handleCharacteristicRead(characteristic, status)
        }

        // 쓴 데이터를 처리하는 함수
        @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            // 쓴 데이터가 CONTROLLER_CHARACTERISTIC_UUID인 경우 successfulDataWrites를 업데이트
            handleCharacteristicWrite(characteristic, status)
        }
    }

    // 연결 상태 변경 처리 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    private fun handleConnectionStateChange(gatt: BluetoothGatt, newState: Int) {
        // 연결 상태가 변경되면 연결 상태를 저장
        val connected = newState == BluetoothGatt.STATE_CONNECTED
        isConnected.value = connected
        // 연결되면 GATT 서비스 목록을 업데이트
        if (connected) services.value = gatt.services
    }

    // 특성 읽기 처리 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    private fun handleCharacteristicRead(characteristic: BluetoothGattCharacteristic, status: Int) {
        if (characteristic.uuid == BLEConstants.CONTROLEE_CHARACTERISTIC_UUID) {
            dataBLERead.value = String(characteristic.value)
            Log.d("BLE", "Characteristic read successful: ${dataBLERead.value}")
            passwordReadCompleted.value = true
        } else {
            Log.e("BLE", "Characteristic read failed for UUID: ${characteristic.uuid}, status: $status")
            passwordReadCompleted.value = false
        }
    }

    // 특성 쓰기 처리 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    private fun handleCharacteristicWrite(characteristic: BluetoothGattCharacteristic, status: Int) {
        if (characteristic.uuid == BLEConstants.CONTROLLER_CHARACTERISTIC_UUID) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                successfulDataWrites.update { it + 1 }
                Log.d("BLE", "Characteristic write successful: ${characteristic.uuid}")

                // Notify that the write was successful
                nameWrittenCompleted.value = true
            } else {
                Log.e("BLE", "Characteristic write failed for UUID: ${characteristic.uuid}, status: $status")
                // Notify that the write failed
                nameWrittenCompleted.value = false
            }
        }
    }

    // GATT 연결을 위한 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun connect() {
        gatt = bluetoothDevice.device.connectGatt(context, false, callback)
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
        Log.d("bluetooth", "ReadCharacteristic")
        val service = gatt?.getService(BLEConstants.UWB_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(BLEConstants.CONTROLEE_CHARACTERISTIC_UUID)
        if (characteristic != null) {
            val success = gatt?.readCharacteristic(characteristic)
            Log.v("bluetooth", "Read status: $success, ${characteristic.value}")
        }
    }

    //  특성 쓰기 함수
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun writeCharacteristic() {
        val service = gatt?.getService(BLEConstants.UWB_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(BLEConstants.CONTROLLER_CHARACTERISTIC_UUID)
        if (characteristic != null) {
            val uwbAddress = uwbCommunicator.getUwbAddress()
            characteristic.value = uwbAddress.toByteArray()
            val success = gatt?.writeCharacteristic(characteristic)
            Log.v("bluetooth", "Write status: $success, ${characteristic.value}")
        }
        if (dataBLERead.value != null) {
            // dataBLERead 값이 null이 아닌지 확인한 후 '/' 기준으로 분할
            val data = dataBLERead.value?.split("/") ?: listOf("", "")
            val address = data.getOrNull(0) ?: ""  // 앞부분이 address
            val channel = data.getOrNull(1) ?: ""  // 뒷부분이 channel

            val success = gatt?.writeCharacteristic(characteristic)

            // UWB 통신 생성, address와 channel로 설정
            uwbCommunicator.startCommunication(address, channel)
            Log.v("uwb", "communication started: $success, Address: $address, Channel: $channel")
        }
    }
}