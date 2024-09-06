package com.alltimes.cartoontime.data.network.uwb

import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbControllerSessionScope
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import com.alltimes.cartoontime.data.model.UwbAddressModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UWBControllerManager(context: Context) {

    private val uwbManager: UwbManager = UwbManager.createInstance(context)
    private val controlees = mutableSetOf<String>()
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val distanceThreshold = 3.0f
    private var isMeasuring = false
    val rangingResultsFlows = mutableMapOf<String, MutableSharedFlow<RangingResult>>()
    private var controllerSessionScope: UwbControllerSessionScope? = null

    init {
        scope.launch {
            initializeUwbSession()
        }
    }

    private suspend fun initializeUwbSession() {
        controllerSessionScope = uwbManager.controllerSessionScope()
        Log.d("UWB", "UWB SESSION INITIALIZED")
    }

    fun UwbConnection(addressModel: UwbAddressModel) {
        scope.launch {
            mutex.withLock {
                val addressAsString = addressModel.getAddressAsString()
                if (controlees.contains(addressAsString)) {
                    return@launch
                }

                controlees.add(addressAsString)
                val uwbAddress = UwbAddress(addressModel.address)
                controllerSessionScope?.addControlee(uwbAddress)
                startCommunication(addressModel)
            }
        }
    }

    fun removeDevice(addressModel: UwbAddressModel) {
        scope.launch {
            mutex.withLock {
                val addressAsString = addressModel.getAddressAsString()
                controlees.remove(addressAsString)
                val uwbAddress = UwbAddress(addressModel.address)
                controllerSessionScope?.removeControlee(uwbAddress)
            }
        }
    }

    fun getUwbAddress(): String {
        val address = controllerSessionScope?.localAddress
        return address?.let { String(it.address) } ?: ""
    }

    fun getUwbChannel(): String {
        return controllerSessionScope?.uwbComplexChannel?.preambleIndex.toString()
    }

    suspend fun startCommunication(addressModel: UwbAddressModel) {
        try {
            val partnerAddress = UwbAddress(addressModel.address)

            if (controllerSessionScope == null) {
                throw IllegalStateException("UWB session not initialized")
            }

            val uwbComplexChannel = controllerSessionScope?.uwbComplexChannel
            val partnerParameters = RangingParameters(
                RangingParameters.CONFIG_MULTICAST_DS_TWR,
                12345,
                0,
                ByteArray(8),
                null,
                uwbComplexChannel,
                listOf(UwbDevice(partnerAddress)),
                RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC
            )

            Log.d("UWB", "LETS START")

            val rangingResultsFlow = rangingResultsFlows.getOrPut(addressModel.getAddressAsString()) {
                MutableSharedFlow(replay = 1)
            }

            controllerSessionScope!!.prepareSession(partnerParameters)
                .flowOn(Dispatchers.IO)
                .onEach { rangingResult ->
                    rangingResultsFlow.emit(rangingResult)
                }
                .launchIn(scope)

            rangingResultsFlow
                .onEach { rangingResult ->
                    Log.d("UWB", "DISTANCE REACHED")
                    if (rangingResult is RangingResult.RangingResultPosition) {
                        val distance = rangingResult.position?.distance?.value
                        if (distance != null) {
                            Log.d("UWB", "DISTANCE: $distance")
                            if (distance <= distanceThreshold) {
                                triggerScreenTransition(addressModel.getAddressAsString())
                            }
                        }
                    }
                }
                .launchIn(scope)

        } catch (e: Exception) {
            Log.e("UWB", "Caught Exception: $e")
        }
    }

    private suspend fun triggerScreenTransition(address: String) {
        isMeasuring = false
        kotlinx.coroutines.delay(5000)
        isMeasuring = true
    }
}
