package com.alltimes.cartoontime.data.model

data class ControleeModel(
    val uwbAddress: UwbAddressModel,
    val isConnected: Boolean,
    val distance: Float?
) {
    // 주소를 String으로 변환하는 헬퍼 함수 추가 가능
    fun getAddressAsString(): String {
        return uwbAddress.getAddressAsString()
    }
}