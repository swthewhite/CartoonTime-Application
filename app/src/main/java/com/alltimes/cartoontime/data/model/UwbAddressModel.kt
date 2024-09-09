package com.alltimes.cartoontime.data.model

data class UwbAddressModel(
    val address: ByteArray
) {
    fun getAddressAsString(): String {
        return String(address)
    }
}