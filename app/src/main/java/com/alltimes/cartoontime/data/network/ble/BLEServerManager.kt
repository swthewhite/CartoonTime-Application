package com.alltimes.cartoontime.data.network.ble

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
import com.alltimes.cartoontime.data.model.BLEConstants
import com.alltimes.cartoontime.data.model.UwbAddressModel
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.uwb.UwbControllerCommunicator
import com.alltimes.cartoontime.ui.viewmodel.BLEServerViewModel
import com.alltimes.cartoontime.ui.viewmodel.UWBControllerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BLEServerManager(private val context: Context, private val viewModel: BLEServerViewModel) {

    private val bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager ?: throw Exception("This device doesn't support Bluetooth")

    private var server: BluetoothGattServer? = null
    private var ctfService: BluetoothGattService? = null

    private var advertiseCallback: AdvertiseCallback? = null
    private val isServerListening: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    private val preparedWrites = HashMap<Int, ByteArray>()
    private val deviceNames = mutableMapOf<String, String>()
    val controllerReceived = MutableStateFlow(emptyList<String>())
    var partnerID: String = ""
    private val uwbCommunicator = UwbControllerCommunicator(context)


    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun startServer() = withContext(Dispatchers.IO) {
        if (server != null) {
            return@withContext
        }

        startHandlingIncomingConnections()
        startAdvertising()
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

                Log.d("BLE", "${characteristic?.uuid}")
                val responseData: ByteArray = when (characteristic?.uuid) {
                    BLEConstants.CONTROLEE_CHARACTERISTIC_UUID -> {
                        Log.d("BLE", "Reading Controlee Characteristic")
                        "$uwbAddress/$uwbChannel".toByteArray()
                    }
                    BLEConstants.RECEIVER_ID_CHARACTERISTIC_UUID -> {
                        Log.d("BLE", "Reading Receiver ID Characteristic")
                        "ct1298".toByteArray()
                    }

                    else -> "UnknownCharacteristic".encodeToByteArray()
                }

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

                when (characteristic.uuid) {
                    BLEConstants.CONTROLLER_CHARACTERISTIC_UUID -> {
                        Log.d("UWB", "Writing to Controller Characteristic")
                        val receivedData = String(value)
                        controllerReceived.update { it.plus(receivedData) }
                    }
                    BLEConstants.SENDER_ID_CHARACTERISTIC_UUID -> {
                        Log.d("UWB", "Writing to Partner ID Characteristic")
                        val receivedData = String(value)
                        // 파트너 ID 처리
                        partnerID = 2344234.toString()
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
                    controllerReceived.update { it.plus(receivedData) }
                    val deviceName = device.name ?: "Unknown Device"
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

        val service = BluetoothGattService(BLEConstants.UWB_KIOSK_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val controleeCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.CONTROLEE_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val myIDCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.RECEIVER_ID_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val controllerCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.CONTROLLER_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val partnerIDCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.SENDER_ID_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(controleeCharacteristic)
        service.addCharacteristic(controllerCharacteristic)
        service.addCharacteristic(myIDCharacteristic)
        service.addCharacteristic(partnerIDCharacteristic)
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
        return parts[0]
    }

    private suspend fun collectControllerReceived() {
        Log.d("UWB", "DATA IS COMING")
        controllerReceived.collect { receivedDataList ->
            if (receivedDataList.isNotEmpty()) {
                val lastReceivedData = receivedDataList.last()
                val address = parseReceivedData(lastReceivedData)
                val deviceName = deviceNames[address] ?: "Unknown Device"

                Log.d("uwb", "me:" + uwbCommunicator.getUwbAddress() + " " + uwbCommunicator.getUwbChannel() + " " + partnerID)
                Log.d("uwb", "controlee:$address")

                // RangingCallback을 viewModel로 연결
                val callback = object : RangingCallback {
                    override fun onDistanceMeasured(distance: Float) {
                        viewModel.onDistanceMeasured(distance) // 뷰모델의 메서드 호출
                    }
                }

                viewModel.setSession(true)
                uwbCommunicator.startCommunication(address, callback)
                //uwbCommunicator.startCommunication(address)//UwbAddressModel(address.toByteArray()).toString())
                //uwbCommunicator.UwbConnection(UwbAddressModel(address.toByteArray()))
            }
        }
    }

    fun disconnectUWB() {
        uwbCommunicator.stopCommunication()
    }
}
