package com.alltimes.cartoontime

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.alltimes.cartoontime.data.repository.FCMRepository
import com.alltimes.cartoontime.ui.viewmodel.ReceiveViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
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

        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    val uid = user?.uid
                    println("익명 사용자 로그인 성공, UID :  ${uid}")

                    saveAnonymousUserId(uid)
                } else {
                    println("익명 사용자 로그인 실패: ${task.exception}")
                }
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

    private fun saveAnonymousUserId(uid: String?) {
        if (uid != null) {
            val sharedPreferences: SharedPreferences =
                getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("anonymousUserId", uid)
            editor.apply()

            println("익명 사용자 UID 저장 완료: $uid")
        }
    }

    fun getFirestore(): FirebaseFirestore {
        return firestore
    }
}