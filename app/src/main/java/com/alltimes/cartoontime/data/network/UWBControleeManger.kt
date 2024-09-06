package com.alltimes.cartoontime.data.network

import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbControleeSessionScope
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class UWBControleeManger(context: Context) {

    private val uwbManager: UwbManager = UwbManager.createInstance(context)

    // 장치 주소를 관리
    private val controller = mutableSetOf<String>()
    private val mutex = Mutex() // 장치 연결 관리 시 동기화
    private val scope = CoroutineScope(Dispatchers.IO) // 비동기 작업을 위한 CoroutineScope
    private val distanceThreshold = 3.0f // 거리 임계값 (예: 3cm)
    private var isMeasuring = false // 거리 측정 여부
    private val rangingResultsFlows = ConcurrentHashMap<String, MutableSharedFlow<RangingResult>>()

    // 비동기 초기화를 위한 변수
    private var controleeSessionScope: UwbControleeSessionScope? = null

    init {
        // 비동기 초기화
        CoroutineScope(Dispatchers.IO).launch {
            initializeUwbSession()
        }
    }

    private suspend fun initializeUwbSession() {
        // 비동기적으로 UWB 세션을 초기화
        controleeSessionScope = uwbManager.controleeSessionScope()
    }

    // BLE를 통해 UWB 연결 요청 시 호출
    fun UwbConnection(address: String, channel: String) {
        scope.launch {
            mutex.withLock {
                if (controller.contains(address)) {
                    return@launch // 이미 존재하면 함수를 종료
                }
                // address가 존재하지 않으면 추가
                controller.add(address)

                // address가 유효한 UwbAddress 형식으로 변환되는지 확인
                val addressBytes = address.toByteArray() // 예제: 실제 변환 로직 필요
                val uwbAddress = UwbAddress(addressBytes)
                // channel이 유효한 UwbComplexChannel 형식으로 변환되는지 확인
                val channelPreamble: Int = channel.toInt()
                val uwbComplexChannel = UwbComplexChannel(9, channelPreamble)

                // 추가 작업 start communication 거리 재기
                startCommunication(address, channel)
            }
        }
    }

    fun removeDevice(address: String, uwbChannel: String) {
        scope.launch {
            mutex.withLock {
                // 장치 제거
                controller.remove(address)

                // address가 유효한 UwbAddress 형식으로 변환되는지 확인
                val addressBytes = address.toByteArray() // 예제: 실제 변환 로직 필요
                val uwbAddress = UwbAddress(addressBytes)

                controleeSessionScope = null
            }
        }
    }

    fun getUwbAddress(): String {
        val address = controleeSessionScope?.localAddress
        return address?.let { String(it.address) } ?: ""
    }

    suspend fun startCommunication(address: String, channel: String) = withContext(Dispatchers.IO) {
        try {
            val partnerAddress = UwbAddress(address.toByteArray()) // UwbAddress 변환 확인
            val partnerChannel = UwbComplexChannel(9, channel.toInt()) // UwbComplexChannel 변환 확인

            if (controleeSessionScope == null) {
                throw IllegalStateException("UWB session not initialized")
            }

            val partnerParameters = RangingParameters(
                RangingParameters.CONFIG_MULTICAST_DS_TWR,
                12345,
                0,
                ByteArray(8),
                null,
                partnerChannel,
                listOf(UwbDevice(partnerAddress)),
                RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC
            )

            Log.d("UWB", "LETS START")

            // Create or get existing MutableSharedFlow for the address
            val rangingResultsFlow = rangingResultsFlows.getOrPut(address) {
                MutableSharedFlow(replay = 1)
            }

            // Flow를 통해 거리 측정 결과를 수집
            controleeSessionScope!!.prepareSession(partnerParameters)
                .flowOn(Dispatchers.IO)
                .onEach { rangingResult ->
                    rangingResultsFlow.emit(rangingResult)
                }
                .launchIn(scope)

            rangingResultsFlow
                .onEach { rangingResult ->
                    Log.d("UWB", "DISTANCE REACHED")
                    when (rangingResult) {
                        is RangingResult.RangingResultPosition -> {
                            Log.d("UWB", "DISTANCE RESULT ON")
                            val distance = rangingResult.position?.distance?.value
                            if (distance != null) {
                                Log.d("UWB", "DISTANCE : $distance")
                                println("Distance: $distance")

                                // 거리 측정 결과가 임계값 이하일 때 특정 작업 수행
                                if (distance <= distanceThreshold) {
                                    // 예: 화면 전환 트리거
                                    triggerScreenTransition(address)
                                }
                            }
                        }
                        else -> {
                            println("CONNECTION LOST")
                        }
                    }
                }
                .launchIn(scope)

        } catch (e: Exception) { // NumberFormatException 외에도 다른 예외 처리
            Log.e("UWB", "Caught Exception: $e")
        }
    }

    private suspend fun triggerScreenTransition(address: String) {
        // 화면 전환 로직을 구현
        // 예: 상대 장치에 화면 전환을 알리고, 현재 장치에서 거리 측정을 잠시 멈추기

        isMeasuring = false
        // 상대 장치에 알림을 보내는 등의 작업 수행

        // 예제: 화면 전환 후 5초 대기
        kotlinx.coroutines.delay(5000)

        // 일정 시간 후 다시 거리 측정을 재개
        isMeasuring = true
    }
}