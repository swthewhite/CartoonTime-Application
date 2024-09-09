package com.alltimes.cartoontime.data.model

import com.alltimes.cartoontime.data.model.UwbAddressModel
import com.alltimes.cartoontime.data.model.UwbChannelModel

data class UwbParametersModel(
    val address: UwbAddressModel,
    val channel: UwbChannelModel,
    val sessionId: Int,
    val updateRate: Int
)