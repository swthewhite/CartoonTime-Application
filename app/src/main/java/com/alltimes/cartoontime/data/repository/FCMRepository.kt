package com.alltimes.cartoontime.data.repository

import com.alltimes.cartoontime.common.MessageListener
import com.alltimes.cartoontime.data.model.fcm.FcmMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FCMRepository(private val listener: MessageListener? = null) {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private var listenerRegistration: ListenerRegistration? = null


    fun listenForMessages(fcmToken: String) {
        println("메시지 수신 중 ... : $fcmToken")
        listenerRegistration = firestore.collection("messages")
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

                    message?.let { listener?.onMessageReceived(it) }
                    deleteMessage(document.id)
                }
            }
    }

    private fun deleteMessage(documentId: String) {
        firestore.collection("messages").document(documentId)
            .delete()
            .addOnSuccessListener {
                println("메시지가 성공적으로 삭제되었습니다. ID: $documentId")
            }
            .addOnFailureListener { e ->
                println("메시지 삭제 실패: ${e.message}")
            }
    }

    //              myFCMToken        otherFCMToken
    fun saveMessage(senderId: String, receiverId: String, content: String) {
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

    // 리스너 제거 메서드
    fun removeMessageListener() {
        listenerRegistration?.remove() // 리스너 제거
        listenerRegistration = null
        println("메시지 리스너가 제거되었습니다.")
    }
}