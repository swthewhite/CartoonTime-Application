package com.alltimes.cartoontime.data.network.ble

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.alltimes.cartoontime.data.model.ble.BLEConstants
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.uwb.UwbController
import com.alltimes.cartoontime.ui.viewmodel.ReceiveViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BLEServerManager(
    private val context: Context,
    private val myIdData: String,
    private val mode: String,
    private val viewModel: ReceiveViewModel,
) {

    private val bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager ?: throw Exception("This device doesn't support Bluetooth")

    private var server: BluetoothGattServer? = null
    private var ctfService: BluetoothGattService? = null

    private var advertiseCallback: AdvertiseCallback? = null
    private val isServerListening: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    private val preparedWrites = HashMap<Int, ByteArray>()
    private val typeWrites = HashMap<Int, String>()
    private val deviceNames = mutableMapOf<String, String>()

    // 변경: controleeReceived와 senderID를 MutableStateFlow로 관리
    var controleeReceived = MutableStateFlow<String?>(null)
    val senderID = MutableStateFlow<String?>(null)
    val uwbStart = MutableStateFlow<String?>(null)

    private val uwbCommunicator = UwbController(context)

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun startServer() = withContext(Dispatchers.IO) {
        if (server != null) {
            return@withContext
        }

        startHandlingIncomingConnections()
        startAdvertising()
        collectUwbStart()
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

        val data = when (mode) {
            "KIOSK" -> {
                // 로그인 모드일 때
                AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .setIncludeTxPowerLevel(false)
                    .addServiceUuid(ParcelUuid(BLEConstants.UWB_KIOSK_SERVICE_UUID))
                    .build()
            }

            "WITCH" -> {
                // 송금 모드일 때
                AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .setIncludeTxPowerLevel(false)
                    .addServiceUuid(ParcelUuid(BLEConstants.UWB_WITCH_SERVICE_UUID))
                    .build()
            }

            else -> null
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

                val uwbAddress = uwbCommunicator.getUwbAddress()
                val uwbChannel = uwbCommunicator.getUwbChannel()

                Log.d("BLE", "Characteristic Read Request: ${characteristic?.uuid}")
                val responseData: ByteArray = when (characteristic?.uuid) {
                    BLEConstants.CONTROLLER_CHARACTERISTIC_UUID -> {
                        Log.d("BLE", "Reading Controller Characteristic")
                        "$uwbAddress/$uwbChannel".toByteArray()
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
                        Log.d("BLE", "Writing to Controlee Characteristic")
                        val receivedData = String(value)
                        Log.d("BLE", "Received data: $receivedData")
                        controleeReceived.value = receivedData
                    }

                    BLEConstants.SENDER_ID_CHARACTERISTIC_UUID -> {
                        Log.d("BLE", "Writing to Sender ID Characteristic")
                        val receivedData = String(value)
                        Log.d("BLE", "Received data: $receivedData")
                        senderID.value = receivedData

                        GlobalScope.launch(Dispatchers.Main) {
                            Log.d("BLE", "Going to RECEIVEDESCRIPTION screen")
                            viewModel.goScreen(ScreenType.RECEIVEDESCRIPTION)
                        }
                    }

                    BLEConstants.UWB_START_CHARACTERISTIC_UUID -> {
                        Log.d("BLE", "Writing to UWB Start Characteristic")
                        val receivedData = String(value)
                        Log.d("BLE", "Received data: $receivedData")
                        if (receivedData == "start") {
                            uwbStart.value = receivedData
                        }
                    }


                    else -> {
                        Log.d("BLE", "Unknown Characteristic UUID")
                    }
                }

                if (preparedWrite) {
                    Log.d("BLE", "Prepared write")
                    val bytes = preparedWrites.getOrDefault(requestId, byteArrayOf())
                    preparedWrites[requestId] = bytes.plus(value)
                } else {
                    Log.d("BLE", "Not prepared write")
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
                    val data = String(bytes)
                    controleeReceived.value = data
                }
            }
        })

        val serviceUuid = when (mode) {
            "KIOSK" -> BLEConstants.UWB_KIOSK_SERVICE_UUID // 로그인 모드
            "WITCH" -> BLEConstants.UWB_WITCH_SERVICE_UUID // 송금 모드
            else -> null
        }

        val service = BluetoothGattService(
            serviceUuid,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

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
        service.addCharacteristic(receiverIDCharacteristic)
        service.addCharacteristic(controleeCharacteristic)
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

    private fun parseReceivedData(data: String): String {
        val parts = data.split("/")
        return parts[0]
    }

    private suspend fun collectControllerReceived() {
        Log.d("BLE", "Collecting data from clients")
        combine(
            controleeReceived.filterNotNull(),
            senderID.filterNotNull(),
        ) { controleeData, senderId ->
            Pair(controleeData, senderId)
        }.collect { (controleeData, senderId) ->
            Log.d("BLE", "Received controleeData: $controleeData, senderId: $senderId")
            if (controleeData.isNotEmpty() && senderId.isNotEmpty()) {
                val address = parseReceivedData(controleeData)

                // RangingCallback을 viewModel로 연결
                val callback = object : RangingCallback {
                    override fun onDistanceMeasured(distance: Float) {
                        viewModel.onDistanceMeasured(distance)
                    }
                }

                viewModel.setSession(true)
            }
        }
    }

    private suspend fun collectUwbStart() {
        Log.d("BLE", "Starting to collect UWB start data")
        uwbStart.filterNotNull().collect { uwbStartData ->
            Log.d("BLE", "Received uwbStartData: $uwbStartData")

            if (uwbStartData.isNotEmpty()) {
                startUwbRanging()
            }
        }
    }

    private fun startUwbRanging() {
        val address = controleeReceived.value ?: return
        val callback = object : RangingCallback {
            override fun onDistanceMeasured(distance: Float) {
                viewModel.onDistanceMeasured(distance)
            }
        }
        viewModel.setSession(true)
        uwbCommunicator.createRanging(address, callback)

        // UI 관련 작업은 Main 스레드에서 실행
        GlobalScope.launch(Dispatchers.Main) {
            viewModel.goScreen(ScreenType.RECEIVELOADING)
        }
    }

    fun disconnectUWB() {
        uwbCommunicator.destroyRanging()
    }
}
