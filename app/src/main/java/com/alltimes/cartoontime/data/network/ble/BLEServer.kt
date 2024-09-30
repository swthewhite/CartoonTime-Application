package com.alltimes.cartoontime.data.network.ble

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.alltimes.cartoontime.data.model.BLEConstants
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.uwb.UwbController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BLEServer(
    private val context: Context,
    private val myUWBChannel: String,
    private val myUWBAddress: String,
    private val myIdData: String,
    private val mode: String
) {

    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager ?: throw Exception("This device doesn't support Bluetooth")

    private var bluetoothGattServer: BluetoothGattServer? = null
    private var service: BluetoothGattService? = null
    private var advertiseCallback: AdvertiseCallback? = null

    // 서버 상태를 추적하는 플로우
    val isServerListening = MutableStateFlow(false)
    val controleeReceived = MutableStateFlow<String?>(null)
    val senderID = MutableStateFlow<String?>(null)
    val uwbStartReceived = MutableStateFlow<Boolean>(false) // UWB Start 수신 여부

    private val uwbCommunicator = UwbController(context) // UWB 통신을 위한 객체

    /**
     * BLE 서버 시작
     */
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun start() = withContext(Dispatchers.IO) {
        if (bluetoothGattServer != null) {
            Log.d("BLEServer", "Server is already running")
            return@withContext
        }

        startHandlingIncomingConnections()
        startAdvertising()
    }

    /**
     * BLE 서버 중지
     */
    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun stop() = withContext(Dispatchers.IO) {
        stopAdvertising()
        stopHandlingIncomingConnections()
    }

    /**
     * BLE 서비스 광고 시작
     */
    @RequiresPermission("android.permission.BLUETOOTH_ADVERTISE")
    private suspend fun startAdvertising() {
        val advertiser: BluetoothLeAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
            ?: throw Exception("This device is not able to advertise")

        if (advertiseCallback != null) {
            Log.d("BLEServer", "Already advertising")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0) // 타임아웃 없음
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
            val callback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    super.onStartSuccess(settingsInEffect)
                    Log.d("BLEServer", "Advertising started successfully")
                    continuation.resume(this)
                }

                override fun onStartFailure(errorCode: Int) {
                    super.onStartFailure(errorCode)
                    Log.e("BLEServer", "Advertising failed to start, error code: $errorCode")
                    continuation.resume(null)
                }
            }
            advertiser.startAdvertising(settings, data, callback)
        }
    }

    /**
     * BLE 서비스 광고 중지
     */
    @RequiresPermission("android.permission.BLUETOOTH_ADVERTISE")
    private fun stopAdvertising() {
        val advertiser: BluetoothLeAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
            ?: throw Exception("This device is not able to advertise")

        advertiseCallback?.let {
            advertiser.stopAdvertising(it)
            advertiseCallback = null
            Log.d("BLEServer", "Advertising stopped")
        }
    }

    /**
     * 수신 연결 및 요청 처리 시작
     */
    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private fun startHandlingIncomingConnections() {
        bluetoothGattServer = bluetoothManager.openGattServer(context, object : BluetoothGattServerCallback() {
            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                super.onServiceAdded(status, service)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    isServerListening.value = true
                    Log.d("BLEServer", "Service added successfully")
                } else {
                    Log.e("BLEServer", "Failed to add service, status: $status")
                }
            }

            /**
             * 읽기 요청 처리
             */
            @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

                val responseData: ByteArray = when (characteristic?.uuid) {
                    BLEConstants.CONTROLLER_CHARACTERISTIC_UUID -> {
                        Log.d("BLEServer", "Reading Controller Characteristic")
                        "$myUWBAddress/$myUWBChannel".toByteArray()
                    }
                    BLEConstants.RECEIVER_ID_CHARACTERISTIC_UUID -> {
                        Log.d("BLEServer", "Reading Receiver ID Characteristic")
                        myIdData.toByteArray()
                    }
                    else -> {
                        Log.w("BLEServer", "Unknown Characteristic UUID: ${characteristic?.uuid}")
                        byteArrayOf()
                    }
                }

                bluetoothGattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    responseData
                )
            }

            /**
             * 쓰기 요청 처리
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
                        val receivedData = String(value)
                        Log.d("BLEServer", "Received Controlee data: $receivedData")
                        controleeReceived.value = receivedData
                    }
                    BLEConstants.SENDER_ID_CHARACTERISTIC_UUID -> {
                        val receivedData = String(value)
                        Log.d("BLEServer", "Received Sender ID: $receivedData")
                        senderID.value = receivedData
                    }
                    BLEConstants.UWB_START_CHARACTERISTIC_UUID -> {
                        val receivedData = String(value)
                        Log.d("BLEServer", "Received UWB Start command: $receivedData")
                        if (receivedData == "start") {
                            uwbStartReceived.value = true
                            startUwbRanging()
                        }
                    }
                    else -> {
                        Log.w("BLEServer", "Unknown Characteristic UUID: ${characteristic.uuid}")
                    }
                }

                if (responseNeeded) {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        offset,
                        byteArrayOf()
                    )
                }
            }

            override fun onConnectionStateChange(
                device: BluetoothDevice?,
                status: Int,
                newState: Int
            ) {
                super.onConnectionStateChange(device, status, newState)
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLEServer", "Device connected: ${device?.address}")
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLEServer", "Device disconnected: ${device?.address}")
                }
            }

        })

        // GATT 서비스 및 특성 설정
        service = createGattService()
        bluetoothGattServer?.addService(service)
    }

    /**
     * 수신 연결 처리 중지
     */
    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private fun stopHandlingIncomingConnections() {
        bluetoothGattServer?.close()
        bluetoothGattServer = null
        isServerListening.value = false
        Log.d("BLEServer", "Server stopped")
    }

    /**
     * GATT 서비스 및 특성 생성
     */
    private fun createGattService(): BluetoothGattService {
        val serviceUuid = when (mode) {
            "KIOSK" -> BLEConstants.UWB_KIOSK_SERVICE_UUID
            "WITCH" -> BLEConstants.UWB_WITCH_SERVICE_UUID
            else -> throw Exception("Invalid mode")
        }

        val service = BluetoothGattService(
            serviceUuid,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        // 특성 정의

        // Controller 특성 (클라이언트에서 읽기)
        val controllerCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.CONTROLLER_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        // Receiver ID 특성 (클라이언트에서 읽기)
        val receiverIDCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.RECEIVER_ID_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        // Controlee 특성 (클라이언트에서 쓰기)
        val controleeCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.CONTROLEE_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        // Sender ID 특성 (클라이언트에서 쓰기)
        val senderIDCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.SENDER_ID_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        // UWB Start 특성 (클라이언트에서 쓰기)
        val uwbStartCharacteristic = BluetoothGattCharacteristic(
            BLEConstants.UWB_START_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        // 특성을 서비스에 추가
        service.addCharacteristic(controllerCharacteristic)
        service.addCharacteristic(receiverIDCharacteristic)
        service.addCharacteristic(controleeCharacteristic)
        service.addCharacteristic(senderIDCharacteristic)
        service.addCharacteristic(uwbStartCharacteristic)

        return service
    }

    /**
     * UWB Ranging 시작
     */
    private fun startUwbRanging() {
        val address = controleeReceived.value
        val senderId = senderID.value

        if (address == null || senderId == null) {
            Log.e("BLEServer", "Cannot start UWB ranging: address or senderID is null")
            return
        }

        val callback = object : RangingCallback {
            override fun onDistanceMeasured(distance: Float) {
                // 거리 측정 결과 처리
                Log.d("BLEServer", "Measured distance: $distance")
                // 필요한 로직을 여기에 추가하세요
            }
        }

        uwbCommunicator.createRanging(address, callback)
    }
}
