package com.alltimes.cartoontime.data.network

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BLEServerManager(private val context: Context) {

    // BluetoothManager 초기화
    private val bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager ?: throw Exception("This device doesn't support Bluetooth")

    // UUID 정의
    private val serviceUuid = UUID.fromString("8c380000-10bd-4fdb-ba21-1922d6cf860d")
    private val passwordCharUuid = UUID.fromString("8c380001-10bd-4fdb-ba21-1922d6cf860d")
    private val nameCharUuid = UUID.fromString("8c380002-10bd-4fdb-ba21-1922d6cf860d")

    private var server: BluetoothGattServer? = null
    private var ctfService: BluetoothGattService? = null

    private var advertiseCallback: AdvertiseCallback? = null
    private val isServerListening: MutableStateFlow<Boolean?> = MutableStateFlow(null)

    private val preparedWrites = HashMap<Int, ByteArray>()

    private val deviceNames = mutableMapOf<String, String>() // 주소와 이름을 매핑할 변수

    val controllerReceived = MutableStateFlow(emptyList<String>())

    private val uwbCommunicator = UWBControllerManager(context)

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun startServer() = withContext(Dispatchers.IO) {
        if (server != null) {
            return@withContext
        }

        startHandlingIncomingConnections()
        startAdvertising()

        // Listen for controllerReceived updates
        collectControllerReceived()
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun stopServer() = withContext(Dispatchers.IO) {
        if (server == null) {
            return@withContext
        }

        stopAdvertising()
        stopHandlingIncomingConnections()
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADVERTISE")
    private suspend fun startAdvertising() {
        val advertiser: BluetoothLeAdvertiser = bluetooth.adapter.bluetoothLeAdvertiser
            ?: throw Exception("This device is not able to advertise")

        if (advertiseCallback != null) {
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)
            .build()

        advertiseCallback = suspendCoroutine { continuation ->
            val advertiseCallback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    super.onStartSuccess(settingsInEffect)
                    continuation.resume(this)
                }

                override fun onStartFailure(errorCode: Int) {
                    super.onStartFailure(errorCode)
                    throw Exception("Unable to start advertising, errorCode: $errorCode")
                }
            }
            advertiser.startAdvertising(settings, data, advertiseCallback)
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADVERTISE")
    private fun stopAdvertising() {
        val advertiser: BluetoothLeAdvertiser = bluetooth.adapter.bluetoothLeAdvertiser
            ?: throw Exception("This device is not able to advertise")

        advertiseCallback?.let {
            advertiser.stopAdvertising(it)
            advertiseCallback = null
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private fun startHandlingIncomingConnections() {
        server = bluetooth.openGattServer(context, object : BluetoothGattServerCallback() {
            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                super.onServiceAdded(status, service)
                isServerListening.value = true
            }

            @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                val uwbAddress = uwbCommunicator.getUwbAddress()
                val uwbChannel = uwbCommunicator.getUwbChannel()

                // UWB 주소와 채널을 결합하여 하나의 ByteArray로 만듭니다.
                val responseData = "$uwbAddress/$uwbChannel".toByteArray()

                server?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, responseData)
            }

            @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                super.onCharacteristicWriteRequest(
                    device,
                    requestId,
                    characteristic,
                    preparedWrite,
                    responseNeeded,
                    offset,
                    value
                )

                if (preparedWrite) {
                    val bytes = preparedWrites.getOrDefault(requestId, byteArrayOf())
                    preparedWrites[requestId] = bytes.plus(value)
                } else {
                    val receivedData = String(value)
                    controllerReceived.update { it.plus(receivedData) }

                    // 장치 이름 저장
                    val deviceName = device?.name ?: "Unknown Device"
                    deviceNames[device.address] = deviceName
                }

                if (responseNeeded) {
                    server?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, byteArrayOf())
                }
            }

            override fun onExecuteWrite(
                device: BluetoothDevice?,
                requestId: Int,
                execute: Boolean
            ) {
                super.onExecuteWrite(device, requestId, execute)
                val bytes = preparedWrites.remove(requestId)
                if (execute && bytes != null) {
                    controllerReceived.update { it.plus(String(bytes)) }
                }
            }
        })

        val service = BluetoothGattService(serviceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val passwordCharacteristic = BluetoothGattCharacteristic(
            passwordCharUuid,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val nameCharacteristic = BluetoothGattCharacteristic(
            nameCharUuid,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(passwordCharacteristic)
        service.addCharacteristic(nameCharacteristic)
        server?.addService(service)
        ctfService = service
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private fun stopHandlingIncomingConnections() {
        ctfService?.let {
            server?.removeService(it)
            ctfService = null
        }
    }

    private fun parseReceivedData(data: String): String {
        val parts = data.split("/")
        val addressPart = parts[0]
        return addressPart
    }

    private suspend fun collectControllerReceived() {
        Log.d("UWB", "DATA IS COMING")
        controllerReceived.collect { receivedDataList ->
            if (receivedDataList.isNotEmpty()) {
                val lastReceivedData = receivedDataList.last()
                val address = parseReceivedData(lastReceivedData)
                val deviceName = deviceNames[address] ?: "Unknown Device"

                Log.d("uwb", "me:" + uwbCommunicator.getUwbAddress() + " " + uwbCommunicator.getUwbChannel())
                Log.d("uwb", "controlee:$address")
                uwbCommunicator.UwbConnection(address)
            }
        }
    }
}
