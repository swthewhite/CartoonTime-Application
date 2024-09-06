package com.alltimes.cartoontime.data.model

class DataInfo {
    val temp: Boolean = false
}

// UI 상태를 나타내는 데이터 클래스
data class UIState(
    // 화면 작동 상태 ( 시작, 정지 => 이 값으로 버튼에 써지는 이름을 바꾸면 됨 )
    val isRunning: Boolean = false
)

// UWB 정보를 나타내는 데이터 클래스
data class UwbInfo(
    // 내 정보
    val uwbAddress: String = null.toString(),
    val uwbChannel: String = null.toString(),
    // 상대 정보
    val partnerAddress: String = null.toString(),
    val partnerChannel: String = null.toString()
)