package com.alltimes.cartoontime.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class FingerprintActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAvailableAuth()
    }

    private fun checkAvailableAuth() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                promptBiometricAuth()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "기기에서 생체 인증을 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "생체 인증 하드웨어를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "생체 인식 정보가 등록되지 않았습니다.", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }
            else -> {
                Toast.makeText(this, "생체 인증을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun promptBiometricAuth() {
        val executor: Executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Authentication succeeded
                setResult(RESULT_OK)
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@FingerprintActivity, "지문 인증 실패", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        // 사용자 인증을 취소한 경우
                        Toast.makeText(this@FingerprintActivity, "지문 인증이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // 기타 인증 오류 처리
                        Toast.makeText(this@FingerprintActivity, "지문 인증 오류: $errString", Toast.LENGTH_SHORT).show()
                    }
                }
                setResult(RESULT_CANCELED)
                finish()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("지문 인증")
            .setSubtitle("지문을 인식하여 로그인하세요")
            .setNegativeButtonText("취소")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}