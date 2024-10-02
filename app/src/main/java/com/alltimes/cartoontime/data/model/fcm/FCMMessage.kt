package com.alltimes.cartoontime.data.model.fcm

import com.google.firebase.Timestamp

data class FcmMessage(
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null // Timestamp에 기본값 추가 (null 허용)
) {
    // 기본 생성자 필요
    constructor() : this("", "", "", null)
}