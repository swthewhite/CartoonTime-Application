package com.alltimes.cartoontime.data.repository

import androidx.core.uwb.RangingResult
import com.alltimes.cartoontime.data.model.UwbAddressModel
import com.alltimes.cartoontime.data.network.uwb.UWBControllerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class UWBRepository(private val uwbControllerManager: UWBControllerManager) {

    fun connectDevice(addressModel: UwbAddressModel) {
        uwbControllerManager.UwbConnection(addressModel)
    }

    fun disconnectDevice(addressModel: UwbAddressModel) {
        uwbControllerManager.removeDevice(addressModel)
    }

    fun getDistanceFlow(addressModel: UwbAddressModel): Flow<RangingResult> {
        return uwbControllerManager.rangingResultsFlows[addressModel.getAddressAsString()] ?: MutableSharedFlow()
    }

    fun getUwbAddress(): String {
        return uwbControllerManager.getUwbAddress()
    }

    fun getUwbChannel(): String {
        return uwbControllerManager.getUwbChannel()
    }
}
