package com.alltimes.cartoontime.data.network.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.alltimes.cartoontime.data.model.BLEConstants
import com.alltimes.cartoontime.data.model.Permissions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

class BLEClient @RequiresPermission("android.permission.BLUETOOTH_CONNECT") constructor(
    private val context: Context,
    private val bluetoothDevice: BluetoothDevice,
    private val myUWBData: String,
    private val myIdData: String,
    private val mode: String,

    ) {
    val isConnected = MutableStateFlow(false)

    val partnerUWBData = MutableStateFlow<String?>(null)
    val partnerIdData = MutableStateFlow<String?>(null)

    private val services = MutableStateFlow<List<BluetoothGattService>>(emptyList())

    private var gatt: BluetoothGatt? = null

    val serviceDiscoveryCompleted = MutableStateFlow(false)

    val controllerReadCompleted = MutableStateFlow(false)
    val receiverIdReadCompleted = MutableStateFlow(false)
    val controleeWriteCompleted = MutableStateFlow(false)
    val senderIdWriteCompleted = MutableStateFlow(false)
    val uwbStartWriteCompleted = MutableStateFlow(false) // 추가된 플래그

    // CompletableDeferred 객체 선언
    private var controleeWriteDeferred: CompletableDeferred<Boolean>? = null
    private var senderIdWriteDeferred: CompletableDeferred<Boolean>? = null
    private var uwbStartWriteDeferred: CompletableDeferred<Boolean>? = null // 추가된 변수

    /**
     * BluetoothGattCallback을 사용하여 BluetoothGatt 객체의 콜백을 정의
     */
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    private val callback = object : BluetoothGattCallback() {
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

        /**
         * 특성 읽기 처리 함수
         */
        @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.uuid == BLEConstants.CONTROLLER_CHARACTERISTIC_UUID) {
                    partnerUWBData.value = String(characteristic.value)
                    controllerReadCompleted.value = true
                    Log.i("BLEClient", "Controller Data read successful: ${partnerUWBData.value}")
                } else if (characteristic.uuid == BLEConstants.RECEIVER_ID_CHARACTERISTIC_UUID) {
                    partnerIdData.value = String(characteristic.value)
                    receiverIdReadCompleted.value = true
                    Log.d("BLE", "Receiver Id read successful: ${partnerIdData.value}")
                }
            } else {
                Log.e(
                    "BLE",
                    "Characteristic read failed for UUID: ${characteristic.uuid}, status: $status"
                )
            }
        }

        /**
         * 특성 쓰기 처리 함수
         */
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    BLEConstants.CONTROLEE_CHARACTERISTIC_UUID -> {
                        controleeWriteCompleted.value = true
                        controleeWriteDeferred?.complete(true)
                        Log.d("BLE", "Controlee write successful")
                    }

                    BLEConstants.SENDER_ID_CHARACTERISTIC_UUID -> {
                        senderIdWriteCompleted.value = true
                        senderIdWriteDeferred?.complete(true)
                        Log.d("BLE", "Sender ID write successful")
                    }

                    BLEConstants.UWB_START_CHARACTERISTIC_UUID -> {
                        uwbStartWriteCompleted.value = true
                        uwbStartWriteDeferred?.complete(true)
                        Log.d("BLE", "UWB Start write successful")
                    }
                }
            } else {
                Log.e(
                    "BLE",
                    "Characteristic write failed for UUID: ${characteristic.uuid}, status: $status"
                )
                when (characteristic.uuid) {
                    BLEConstants.CONTROLEE_CHARACTERISTIC_UUID -> {
                        controleeWriteCompleted.value = false
                        controleeWriteDeferred?.complete(false)
                    }

                    BLEConstants.SENDER_ID_CHARACTERISTIC_UUID -> {
                        senderIdWriteCompleted.value = false
                        senderIdWriteDeferred?.complete(false)
                    }

                    BLEConstants.UWB_START_CHARACTERISTIC_UUID -> {
                        uwbStartWriteCompleted.value = false
                        uwbStartWriteDeferred?.complete(false)
                    }
                }
            }
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

    // GATT 연결을 위한 함수에 코루틴 추가
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun connect(): Boolean = suspendCancellableCoroutine { continuation ->
        gatt = bluetoothDevice.connectGatt(context, false, callback)

        // 연결 상태가 업데이트되면 콜백에서 resume
        val job: Job = isConnected.onEach { connected ->
            if (connected) {
                continuation.resume(true)
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))  // 백그라운드에서 flow 관찰

        // 코루틴 취소 시 GATT 연결 해제
        continuation.invokeOnCancellation {
            gatt?.disconnect()
            gatt?.close()
            job.cancel()  // collect 중단
        }
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

    /**
     * 특성 읽기 함수
     */
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun readCharacteristic(
        characteristicUUID: UUID,
        readCompletedFlag: MutableStateFlow<Boolean>
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val service = when (mode) {
            "WITCH" -> gatt?.getService(BLEConstants.UWB_WITCH_SERVICE_UUID)
            "KIOSK" -> gatt?.getService(BLEConstants.UWB_KIOSK_SERVICE_UUID)
            else -> null
        }
        val characteristic = service?.getCharacteristic(characteristicUUID)

        if (characteristic != null) {
            val success = gatt?.readCharacteristic(characteristic)
            Log.v("bluetooth", "Read status: $success for UUID: $characteristicUUID")

            val job: Job = readCompletedFlag.onEach { completed ->
                if (completed) {
                    continuation.resume(true)
                }
            }.launchIn(CoroutineScope(Dispatchers.IO))

            continuation.invokeOnCancellation { job.cancel() }
        } else {
            Log.e("bluetooth", "Characteristic UUID $characteristicUUID not found")
            continuation.resume(false)
        }
    }

    /**
     * 특성 쓰기 함수 (공통)
     */
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    private suspend fun writeCharacteristic(
        characteristicUUID: UUID,
        data: ByteArray,
        writeCompletedFlag: MutableStateFlow<Boolean>,
        writeDeferred: CompletableDeferred<Boolean>
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val service = when (mode) {
            "WITCH" -> gatt?.getService(BLEConstants.UWB_WITCH_SERVICE_UUID)
            "KIOSK" -> gatt?.getService(BLEConstants.UWB_KIOSK_SERVICE_UUID)
            else -> null
        }
        val characteristic = service?.getCharacteristic(characteristicUUID)

        if (characteristic != null) {
            characteristic.value = data

            writeCompletedFlag.value = false // 플래그 초기화

            if (gatt?.writeCharacteristic(characteristic) == true) {
                Log.v("bluetooth", "Write started for UUID: $characteristicUUID")

                val job = writeCompletedFlag.onEach { completed ->
                    if (completed) {
                        continuation.resume(true)
                    }
                }.launchIn(CoroutineScope(Dispatchers.IO))

                continuation.invokeOnCancellation { job.cancel() }
            } else {
                Log.e("bluetooth", "Failed to initiate write for UUID: $characteristicUUID")
                continuation.resume(false)
            }
        } else {
            Log.e("bluetooth", "Characteristic UUID $characteristicUUID not found")
            continuation.resume(false)
        }
    }

    /**
     * Sender ID 특성에 데이터를 쓰는 함수
     */
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun writeSenderIdCharacteristic(): Boolean {
        senderIdWriteDeferred = CompletableDeferred()
        return writeCharacteristic(
            BLEConstants.SENDER_ID_CHARACTERISTIC_UUID,
            myIdData.toByteArray(),
            senderIdWriteCompleted,
            senderIdWriteDeferred!!
        )
    }

    /**
     * Controlee 특성에 데이터를 쓰는 함수
     */
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun writeControleeCharacteristic(): Boolean {
        controleeWriteDeferred = CompletableDeferred()
        return writeCharacteristic(
            BLEConstants.CONTROLEE_CHARACTERISTIC_UUID,
            myUWBData.toByteArray(),
            controleeWriteCompleted,
            controleeWriteDeferred!!
        )
    }

    /**
     * UWB Start 특성에 데이터를 쓰는 함수
     */
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun writeUwbStartCharacteristic(): Boolean {
        uwbStartWriteDeferred = CompletableDeferred()
        return writeCharacteristic(
            BLEConstants.UWB_START_CHARACTERISTIC_UUID,
            "start".toByteArray(),
            uwbStartWriteCompleted,
            uwbStartWriteDeferred!!
        )
    }

    /**
     * 필요 데이터 통신 함수
     */
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun communicate(): Boolean {
        // 플래그 초기화
        controleeWriteCompleted.value = false
        receiverIdReadCompleted.value = false

        // 첫 번째 특성 읽기
        val firstSuccess = readCharacteristic(
            BLEConstants.CONTROLLER_CHARACTERISTIC_UUID,
            controllerReadCompleted
        )
        if (!firstSuccess) {
            Log.e("bluetooth", "Failed to read first characteristic")
            return false
        }
        Log.d("bluetooth", "First characteristic read successfully")

        // 두 번째 특성 읽기
        val secondSuccess = readCharacteristic(
            BLEConstants.RECEIVER_ID_CHARACTERISTIC_UUID,
            receiverIdReadCompleted
        )
        if (!secondSuccess) {
            Log.e("bluetooth", "Failed to read second characteristic")
            return false
        }
        Log.d("bluetooth", "Second characteristic read successfully")

        // Controlee 쓰기
        val writeControleeSuccess = writeControleeCharacteristic()
        if (!writeControleeSuccess) {
            Log.e("bluetooth", "Failed to write Controlee characteristic")
            return false
        }
        Log.d("bluetooth", "Controlee characteristic written successfully")

        // Sender ID 쓰기
        val writeSenderIdSuccess = writeSenderIdCharacteristic()
        if (!writeSenderIdSuccess) {
            Log.e("bluetooth", "Failed to write Sender ID characteristic")
            return false
        }
        Log.d("bluetooth", "Sender ID characteristic written successfully")

        return true
    }

    /**
     * UWB 시작 함수
     */
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun startUwbRanging(): Boolean {
        // UWB Start 쓰기
        val uwbStartSuccess = writeUwbStartCharacteristic()
        if (!uwbStartSuccess) {
            Log.e("bluetooth", "Failed to write UWB Start characteristic")
            return false
        }
        Log.d("bluetooth", "UWB Start characteristic written successfully")

        return true
    }

    private suspend fun terminate() {
        // 연결 해제

    }
}
