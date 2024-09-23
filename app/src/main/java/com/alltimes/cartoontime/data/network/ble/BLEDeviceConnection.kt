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
import com.alltimes.cartoontime.data.model.Permissions
import com.alltimes.cartoontime.data.model.BLEConstants
import com.alltimes.cartoontime.data.model.uwb.RangingCallback
import com.alltimes.cartoontime.data.network.uwb.UwbControleeCommunicator
import com.alltimes.cartoontime.ui.viewmodel.BLEScannerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

@Suppress("DEPRECATION")
class BLEDeviceConnection @RequiresPermission("PERMISSION_BLUETOOTH_CONNECT") constructor(
    // Context와 BluetoothDevice를 받아서 초기화
    private val context: Context,
    private val bluetoothDevice: DeviceInfo,
    private val viewModel: BLEScannerViewModel
) {
    // 연결 상태를 저장하는 MutableStateFlow
    val isConnected = MutableStateFlow(false)
    // 읽은 데이터를 저장하는 MutableStateFlow
    val uwbData = MutableStateFlow<String?>(null)
    val idData = MutableStateFlow<String?>(null)

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
    val controleeReadCompleted = MutableStateFlow(false)
    val receiverIdReadCompleted = MutableStateFlow(false)
    val controllerWrittenCompleted = MutableStateFlow(false)
    val senderIdWrittenCompleted = MutableStateFlow(false)

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
        // BluetoothGattCallback 내부에서 두 번째 읽기 작업 완료 후 쓰기 작업 실행
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.uuid == BLEConstants.CONTROLEE_CHARACTERISTIC_UUID) {
                    uwbData.value = String(characteristic.value)
                    controleeReadCompleted.value = true
                    Log.d("BLE", "Controlee read successful: ${uwbData.value}")
                } else if (characteristic.uuid == BLEConstants.RECEIVER_ID_CHARACTERISTIC_UUID) {
                    idData.value = String(characteristic.value)
                    receiverIdReadCompleted.value = true
                    Log.d("BLE", "Receiver id read successful: ${idData.value}")
                }
            } else {
                Log.e("BLE", "Characteristic read failed for UUID: ${characteristic.uuid}, status: $status")
                // 필요 시 플래그를 false로 설정
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.uuid == BLEConstants.CONTROLLER_CHARACTERISTIC_UUID) {
                    controllerWrittenCompleted.value = true
                    Log.d("BLE", "Characteristic write successful: ${characteristic.uuid}")
                } else if (characteristic.uuid == BLEConstants.SENDER_ID_CHARACTERISTIC_UUID) {
                    senderIdWrittenCompleted.value = true
                    Log.d("BLE", "Sender ID write successful: ${characteristic.uuid}")
                }
            } else {
                Log.e("BLE", "Characteristic write failed for UUID: ${characteristic.uuid}, status: $status")
                if (characteristic.uuid == BLEConstants.CONTROLLER_CHARACTERISTIC_UUID) {
                    controllerWrittenCompleted.value = false
                } else if (characteristic.uuid == BLEConstants.SENDER_ID_CHARACTERISTIC_UUID) {
                    senderIdWrittenCompleted.value = false
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
        gatt = bluetoothDevice.device.connectGatt(context, false, callback)

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

    // 특성 읽기 함수 (첫 번째, 두 번째 모두 사용)
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun readCharacteristic(
        characteristicUUID: UUID,
        readCompletedFlag: MutableStateFlow<Boolean>
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val service = gatt?.getService(BLEConstants.UWB_KIOSK_SERVICE_UUID)
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

    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun writeSenderIdCharacteristic(): Boolean = suspendCancellableCoroutine { continuation ->
        val service = gatt?.getService(BLEConstants.UWB_KIOSK_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(BLEConstants.SENDER_ID_CHARACTERISTIC_UUID)

        if (characteristic != null) {
            val senderId = "ct9876"
            characteristic.value = senderId.toByteArray()

            senderIdWrittenCompleted.value = false // 플래그 초기화

            if (gatt?.writeCharacteristic(characteristic) == true) {
                Log.v("bluetooth", "Write started for UUID: ${BLEConstants.SENDER_ID_CHARACTERISTIC_UUID}")

                val job = senderIdWrittenCompleted.onEach { completed ->
                    if (completed) {
                        continuation.resume(true)
                    }
                }.launchIn(CoroutineScope(Dispatchers.IO))

                continuation.invokeOnCancellation { job.cancel() }
            } else {
                Log.e("bluetooth", "Failed to initiate write for UUID: ${BLEConstants.SENDER_ID_CHARACTERISTIC_UUID}")
                continuation.resume(false)
            }
        } else {
            Log.e("bluetooth", "SENDER_ID_CHARACTERISTIC_UUID not found")
            continuation.resume(false)
        }
    }

    // writeControllerCharacteristic 함수 수정
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun writeControllerCharacteristic(): Boolean = suspendCancellableCoroutine { continuation ->
        val service = gatt?.getService(BLEConstants.UWB_KIOSK_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(BLEConstants.CONTROLLER_CHARACTERISTIC_UUID)

        if (characteristic != null) {
            val uwbAddress = uwbCommunicator.getUwbAddress()
            characteristic.value = uwbAddress.toByteArray()

            controllerWrittenCompleted.value = false // 플래그 초기화

            if (gatt?.writeCharacteristic(characteristic) == true) {
                Log.v("bluetooth", "Write started for UUID: ${BLEConstants.CONTROLLER_CHARACTERISTIC_UUID}")

                val job = controllerWrittenCompleted.onEach { completed ->
                    if (completed) {
                        continuation.resume(true)
                    }
                }.launchIn(CoroutineScope(Dispatchers.IO))

                continuation.invokeOnCancellation { job.cancel() }
            } else {
                Log.e("bluetooth", "Failed to initiate write for UUID: ${BLEConstants.CONTROLLER_CHARACTERISTIC_UUID}")
                continuation.resume(false)
            }
        } else {
            Log.e("bluetooth", "CONTROLLER_CHARACTERISTIC_UUID not found")
            continuation.resume(false)
        }
    }

    // 모든 작업을 순차적으로 완료하는 함수 (특성 읽기 및 쓰기)
    @RequiresPermission(Permissions.BLUETOOTH_CONNECT)
    suspend fun performCharacteristicOperationsSequentially(): Boolean {
        // 플래그 초기화
        controleeReadCompleted.value = false
        receiverIdReadCompleted.value = false

        // 첫 번째 특성 읽기
        val firstSuccess = readCharacteristic(
            BLEConstants.CONTROLEE_CHARACTERISTIC_UUID,
            controleeReadCompleted
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

        // **Sender ID 쓰기**
        val writeSenderIdSuccess = writeSenderIdCharacteristic()
        if (!writeSenderIdSuccess) {
            Log.e("bluetooth", "Failed to write Sender ID characteristic")
            return false
        }
        Log.d("bluetooth", "Sender ID characteristic written successfully")

        // **Controller 쓰기**
        val writeControllerSuccess = writeControllerCharacteristic()
        if (!writeControllerSuccess) {
            Log.e("bluetooth", "Failed to write Controller characteristic")
            return false
        }
        Log.d("bluetooth", "Controller characteristic written successfully")

        // UWB 통신 시작
        startUwbCommunication()

        return true
    }

    private fun startUwbCommunication() {
        if (uwbData.value != null) {
            val id = idData.value ?: ""
            Log.v("bluetooth", "idData: $id")

            val data = uwbData.value?.split("/") ?: listOf("", "")
            val address = data.getOrNull(0) ?: ""
            val channel = data.getOrNull(1) ?: ""

            val callback = object : RangingCallback {
                override fun onDistanceMeasured(distance: Float) {
                    viewModel.onDistanceMeasured(distance)
                }
            }

            viewModel.setSession(true)
            uwbCommunicator.startCommunication(address, channel, callback)
            Log.v("uwb", "communication started, Address: $address, Channel: $channel")
        }
    }

    fun disconnectUWB() {
        uwbCommunicator.stopCommunication()
    }
}
