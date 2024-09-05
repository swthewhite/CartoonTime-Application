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

val UWB_SERVICE_UUID: UUID = UUID.fromString("8c380000-10bd-4fdb-ba21-1922d6cf860d")
val CONTROLEE_CHARACTERISTIC_UUID: UUID = UUID.fromString("8c380001-10bd-4fdb-ba21-1922d6cf860d")
val CONTROLLER_CHARACTERISTIC_UUID: UUID = UUID.fromString("8c380002-10bd-4fdb-ba21-1922d6cf860d")

@Suppress("DEPRECATION")
class BLEDeviceConnection @RequiresPermission("PERMISSION_BLUETOOTH_CONNECT") constructor(
    private val context: Context,
    private val bluetoothDevice: BluetoothDevice
) {
    val isConnected = MutableStateFlow(false)
    val passwordRead = MutableStateFlow<String?>(null)
    val successfulNameWrites = MutableStateFlow(0)
    val services = MutableStateFlow<List<BluetoothGattService>>(emptyList())

    private val callback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val connected = newState == BluetoothGatt.STATE_CONNECTED
            if (connected) {
                //read the list of services
                services.value = gatt.services
            }
            isConnected.value = connected
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            services.value = gatt.services
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (characteristic.uuid == CONTROLEE_CHARACTERISTIC_UUID) {
                passwordRead.value = String(characteristic.value)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (characteristic.uuid == CONTROLLER_CHARACTERISTIC_UUID) {
                successfulNameWrites.update { it + 1 }
            }
        }
    }

    private var gatt: BluetoothGatt? = null

    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun connect() {
        gatt = bluetoothDevice.connectGatt(context, false, callback)
    }

    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun discoverServices() {
        gatt?.discoverServices()
    }

    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun readPassword() {
        val service = gatt?.getService(UWB_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CONTROLEE_CHARACTERISTIC_UUID)
        if (characteristic != null) {
            val success = gatt?.readCharacteristic(characteristic)
            Log.v("bluetooth", "Read status: $success")
        }
    }

    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    fun writeName() {
        val service = gatt?.getService(UWB_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CONTROLLER_CHARACTERISTIC_UUID)
        if (characteristic != null) {
            characteristic.value = "CONTROLLER".toByteArray()
            val success = gatt?.writeCharacteristic(characteristic)
            Log.v("bluetooth", "Write status: $success")
        }
    }
}