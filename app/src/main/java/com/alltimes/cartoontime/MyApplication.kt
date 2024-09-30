package com.alltimes.cartoontime

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.alltimes.cartoontime.data.repository.FCMRepository
import com.alltimes.cartoontime.ui.viewmodel.ReceiveViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MyApplication : Application() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()

        // Firebase 초기화
        FirebaseApp.initializeApp(this)
        firestore = FirebaseFirestore.getInstance()

        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    println("FCM 토큰 가져오기 실패: ${task.exception}")
                    return@addOnCompleteListener
                }

                // 새 FCM 토큰
                val token = task.result
                println("FCM 토큰: $token")

                // FCM 토큰 저장 (SharedPreferences나 서버 API 사용)
                saveFcmToken(token)

            }

        println("초기화 완료")
    }

    private fun saveFcmToken(token: String) {
        // SharedPreferences에 FCM 토큰 저장
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("fcmToken", token)
        editor.apply()

        println("FCM 토큰 저장 완료: $token")
    }

    fun getFirestore(): FirebaseFirestore {
        return firestore
    }
}