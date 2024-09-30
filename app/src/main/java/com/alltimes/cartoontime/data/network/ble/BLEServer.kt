package com.alltimes.cartoontime.data.network.ble

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.alltimes.cartoontime.data.model.BLEConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BLEServer constructor(
    private val context: Context,
    private val myUWBChannel: String,
    private val myUWBAddress: String,
    private val myIdData: String,
    private val mode: String
){

    private val bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager ?: throw Exception("This device doesn't support Bluetooth")

    private var server: BluetoothGattServer? = null
    private var ctfService: BluetoothGattService? = null

    private var advertiseCallback: AdvertiseCallback? = null
    private val isServerListening: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    private val preparedWrites = HashMap<Int, ByteArray>()
    private val typeWrites = HashMap<Int, String>()
    private val deviceNames = mutableMapOf<String, String>()

    var controleeReceived = MutableStateFlow<String?>(null)
    val senderID = MutableStateFlow<String?>(null)

    /**
     * Start the BLE server
     */
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun start() = withContext(Dispatchers.IO) {
        if (server != null) {
            return@withContext
        }

        startHandlingIncomingConnections()
        startAdvertising()
    }

    /**
     * Stop the BLE server
     */
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun stop() = withContext(Dispatchers.IO) {
        if (server == null) {
            return@withContext
        }

        stopAdvertising()
        stopHandlingIncomingConnections()
    }

    /**
     * Get the server listening state
     */
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

        val data = when (mode) {
            "KIOSK" -> {
                AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .setIncludeTxPowerLevel(false)
                    .addServiceUuid(ParcelUuid(BLEConstants.UWB_KIOSK_SERVICE_UUID))
                    .build()
            }
            "WITCH" -> {
                AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .setIncludeTxPowerLevel(false)
                    .addServiceUuid(ParcelUuid(BLEConstants.UWB_WITCH_SERVICE_UUID))
                    .build()
            }
            else -> {
                throw Exception("Invalid mode")
            }
        }

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

    /**
     * Stop advertising
     */
    @RequiresPermission("android.permission.BLUETOOTH_ADVERTISE")
    private fun stopAdvertising() {
        val advertiser: BluetoothLeAdvertiser = bluetooth.adapter.bluetoothLeAdvertiser
            ?: throw Exception("This device is not able to advertise")

        advertiseCallback?.let {
            advertiser.stopAdvertising(it)
            advertiseCallback = null
        }
    }

    /**
     * Start handling incoming connections
     */
    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private fun startHandlingIncomingConnections() {
        server = bluetooth.openGattServer(context, object : BluetoothGattServerCallback() {
            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                super.onServiceAdded(status, service)
                isServerListening.value = true
            }

            /**
             * Handle incoming `read` requests
             */
            @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

                Log.d("BLE", "${characteristic?.uuid}")
                val responseData: ByteArray = when (characteristic?.uuid) {
                    BLEConstants.CONTROLLER_CHARACTERISTIC_UUID -> {
                        Log.d("BLE", "Reading Controller Characteristic")
                        "$myUWBAddress/$myUWBChannel".toByteArray()
                    }

                    BLEConstants.RECEIVER_ID_CHARACTERISTIC_UUID -> {
                        Log.d("BLE", "Reading Receiver ID Characteristic")
                        myIdData.toByteArray()
                    }

                    else -> "UnknownCharacteristic".encodeToByteArray()
                }

                server?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    responseData
                )
            }

            /**
             * Handle incoming `write` requests
             */
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

                when (characteristic.uuid) {
                    BLEConstants.CONTROLEE_CHARACTERISTIC_UUID -> {
                        Log.d("UWB", "Writing to Controlee Characteristic")
                        val receivedData = String(value)
                        controleeReceived.value = receivedData
                    }

                    BLEConstants.SENDER_ID_CHARACTERISTIC_UUID -> {
                        Log.d("UWB", "Writing to Sender ID Characteristic")
                        val receivedData = String(value)
                        senderID.value = receivedData
                    }

                    else -> {
                        Log.d("UWB", "Unknown Characteristic UUID")
                    }
                }

                if (preparedWrite) {
                    Log.d("UWB", "Prepared write")
                    val bytes = preparedWrites.getOrDefault(requestId, byteArrayOf())
                    preparedWrites[requestId] = bytes.plus(value)
                } else {
                    Log.d("UWB", "Not prepared write")
                    val receivedData = String(value)
                    controleeReceived.update { it.plus(receivedData) }
                    val deviceName = device.name ?: "Unknown Device"
                    deviceNames[device.address] = deviceName
                }

                if (responseNeeded) {
                    server?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        byteArrayOf()
                    )
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
                    if (typeWrites[requestId] == "CONTROLEE") {
                        controleeReceived.update { it.plus(String(bytes)) }
                    }
                    controleeReceived.update { it.plus(String(bytes)) }
                }
            }
        })

        val service = when (mode) {
            "KIOSK" -> {
                BluetoothGattService(
                    BLEConstants.UWB_KIOSK_SERVICE_UUID,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY
                )
            }
            "WITCH" -> {
                BluetoothGattService(
                    BLEConstants.UWB_WITCH_SERVICE_UUID,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY
                )
            }
            else -> {
                throw Exception("Invalid mode")
            }
        }

        val controllerCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.CONTROLLER_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val receiverIDCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.RECEIVER_ID_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val controleeCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.CONTROLEE_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val senderIDCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.SENDER_ID_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val uwbStartCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.UWB_START_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(controllerCharacteristic)
        service.addCharacteristic(controleeCharacteristic)
        service.addCharacteristic(receiverIDCharacteristic)
        service.addCharacteristic(senderIDCharacteristic)
        service.addCharacteristic(uwbStartCharacteristic)
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
}