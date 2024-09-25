package com.alltimes.cartoontime.data.messaging

import android.annotation.SuppressLint
import android.util.Log
import com.alltimes.cartoontime.data.repository.FCMRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessageService : FirebaseMessagingService() {

    private val fcmRepository = FCMRepository()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // FCM 메시지 수신 시 처리 로직
        remoteMessage.data["type"]?.let { type ->
            when (type) {
                "entry_exit" -> {
                    // 입퇴실 관련 알림을 처리
                    handleEntryExitNotification()
                    println("입퇴실 알림 수신")
                }
                "money_transfer" -> {
                    // 송금 관련 알림을 처리
                    handleMoneyTransferNotification(remoteMessage.data["message"])
                    println("송금 알림 수신: ${remoteMessage.data["message"]}")
                }
                else -> {
                    Log.d("FCMService", "알 수 없는 메시지 유형: $type")
                }
            }
        }
    }

    private fun handleEntryExitNotification() {
        // 여기에 입퇴실 알림 처리 로직 작성
        Log.d("FCMService", "입퇴실 알림 수신")
    }

    private fun handleMoneyTransferNotification(message: String?) {
        // 여기에 송금 알림 처리 로직 작성
        Log.d("FCMService", "송금 알림 수신: $message")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 새로운 FCM 토큰 수신 시 처리 로직
        Log.d("FCMService", "새로운 FCM 토큰: $token")

        // FCM 토큰을 서버에 저장하는 로직 추가
        //fcmRepository.saveToken(token)
    }
}