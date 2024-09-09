package com.alltimes.cartoontime.data.model

// UI 상태를 나타내는 데이터 클래스
data class UIStateModel(
    // 화면 작동 상태 ( 시작, 정지 => 이 값으로 버튼에 써지는 이름을 바꾸면 됨 )
    val isRunning: Boolean = false
)
