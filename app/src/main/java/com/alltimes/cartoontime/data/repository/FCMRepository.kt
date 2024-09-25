package com.alltimes.cartoontime.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.alltimes.cartoontime.data.model.FcmMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FCMRepository {
    private val firestore: FirebaseFirestore = Firebase.firestore

    fun listenForMessages(fcmToken: String) {
        println("메시지 수신 중 ... : $fcmToken")
        firestore.collection("messages")
            .whereEqualTo("receiverId", fcmToken) // FCM 토큰을 사용하여 필터링
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("Listen failed: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    println("해당 토큰과 관련된 메시지가 없습니다.")
                    return@addSnapshotListener
                }

                snapshots.documents.forEach { document ->
                    val message = document.toObject(FcmMessage::class.java)
                    // 메시지 처리 로직 (UI 업데이트 등)
                    println("새 메시지 수신: $message")
                }
            }
    }

    //              myFCMToken        otherFCMToken
    fun saveMessage(senderId: String, receiverId: String, content: String) {
        // 현재 시간을 Timestamp로 변환
        val timestamp = Timestamp.now()
        val message = FcmMessage(senderId, receiverId, content, timestamp)

        firestore.collection("messages")
            .add(message)
            .addOnSuccessListener { documentReference ->
                println("메시지가 성공적으로 저장되었습니다. ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("메시지 저장 실패: ${e.message}")
            }
    }
}