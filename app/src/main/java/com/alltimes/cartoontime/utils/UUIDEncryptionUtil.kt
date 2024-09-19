package com.alltimes.cartoontime.utils

import android.annotation.SuppressLint
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object UUIDEncryptionUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES"

    // UUID를 암호화하는 함수
    @SuppressLint("GetInstance")
    fun encrypt(uuid: String, secretKey: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secretKey))
        val encryptedBytes = cipher.doFinal(uuid.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    // 암호화된 UUID를 복호화하는 함수
    @SuppressLint("GetInstance")
    fun decrypt(encryptedUUID: String, secretKey: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretKey))
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedUUID, Base64.DEFAULT))
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    // 비밀키를 생성하는 함수
    private fun getSecretKey(secretKey: String): Key {
        return SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
    }
}
