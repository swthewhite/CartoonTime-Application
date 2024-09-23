package com.alltimes.cartoontime.data.repository

import com.alltimes.cartoontime.data.remote.ApiService
import com.alltimes.cartoontime.data.remote.AuthRequest
import com.alltimes.cartoontime.data.remote.AuthResponse
import com.alltimes.cartoontime.data.remote.ChargeRequest
import com.alltimes.cartoontime.data.remote.ChargeResponse
import com.alltimes.cartoontime.data.remote.EntryLogResponse
import com.alltimes.cartoontime.data.remote.NaverAuthRequest
import com.alltimes.cartoontime.data.remote.NavertAuthResponse
import com.alltimes.cartoontime.data.remote.PayRequest
import com.alltimes.cartoontime.data.remote.PayResponse
import com.alltimes.cartoontime.data.remote.SignInRequest
import com.alltimes.cartoontime.data.remote.SignResponse
import com.alltimes.cartoontime.data.remote.SignUpRequest
import com.alltimes.cartoontime.data.remote.TransferRequest
import com.alltimes.cartoontime.data.remote.TransferResponse
import com.alltimes.cartoontime.data.remote.UserResponse
import com.alltimes.cartoontime.data.remote.VerifyAuthRequest
import com.alltimes.cartoontime.data.remote.VerifyAuthResponse

class UserRepository(private val apiService: ApiService) {
    suspend fun requestAuthCode(phoneNumber: String): AuthResponse {
        return apiService.requestAuthCode(AuthRequest(phoneNumber))
    }

    suspend fun verifyAuthCode(phoneNumber: String, authCode: String): VerifyAuthResponse {
        return apiService.verifyAuthCode(VerifyAuthRequest(phoneNumber, authCode))
    }

    suspend fun signUp(phoneNumber: String, name: String): SignResponse {
        return apiService.signUp(SignUpRequest(phoneNumber, name))
    }

    suspend fun signIn(phoneNumber: String): SignResponse {
        return apiService.signIn(SignInRequest(phoneNumber))
    }

    suspend fun getUserInfo(userId: Long): UserResponse {
        return apiService.getUserInfo(userId)
    }

    suspend fun naverAuth(request: NaverAuthRequest): NavertAuthResponse {
        return apiService.naverAuth(request)
    }

    suspend fun charge(request: ChargeRequest): ChargeResponse {
        return apiService.charge(request)
    }

    suspend fun transfer(request: TransferRequest): TransferResponse {
        return apiService.transfer(request)
    }

    suspend fun pay(request: PayRequest): PayResponse {
        return apiService.pay(request)
    }

    suspend fun getEntryLog(userId: Long): EntryLogResponse {
        return apiService.getEntryLog(userId)
    }
}
