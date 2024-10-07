package com.alltimes.cartoontime.data.repository

import com.alltimes.cartoontime.data.remote.ApiService
import com.alltimes.cartoontime.data.remote.AuthRequest
import com.alltimes.cartoontime.data.remote.AuthResponse
import com.alltimes.cartoontime.data.remote.ChargeRequest
import com.alltimes.cartoontime.data.remote.ChargeResponse
import com.alltimes.cartoontime.data.remote.ComicResponse
import com.alltimes.cartoontime.data.remote.ComicSearchResponse
import com.alltimes.cartoontime.data.remote.FCMRequest
import com.alltimes.cartoontime.data.remote.FCMResponse
import com.alltimes.cartoontime.data.remote.NaverAuthRequest
import com.alltimes.cartoontime.data.remote.NavertAuthResponse
import com.alltimes.cartoontime.data.remote.Recommendation
import com.alltimes.cartoontime.data.remote.SignInRequest
import com.alltimes.cartoontime.data.remote.SignResponse
import com.alltimes.cartoontime.data.remote.SignUpRequest
import com.alltimes.cartoontime.data.remote.TransferRequest
import com.alltimes.cartoontime.data.remote.TransferResponse
import com.alltimes.cartoontime.data.remote.UserComicRecommendResponse
import com.alltimes.cartoontime.data.remote.UserResponse
import com.alltimes.cartoontime.data.remote.VerifyAuthRequest
import com.alltimes.cartoontime.data.remote.VerifyAuthResponse

class UserRepository(private val apiService: ApiService) {
    suspend fun requestAuthCode(phoneNumber: String): AuthResponse {
        return apiService.requestAuthCode(AuthRequest(phoneNumber))
    }

    suspend fun verifyAuthCode(verifyAuthRequest: VerifyAuthRequest): VerifyAuthResponse {
        return apiService.verifyAuthCode(verifyAuthRequest)
    }

    suspend fun signUp(phoneNumber: String, name: String): SignResponse {
        return apiService.signUp(SignUpRequest(phoneNumber, name))
    }

    suspend fun signIn(phoneNumber: String): SignResponse {
        return apiService.signIn(SignInRequest(phoneNumber))
    }

    suspend fun saveFcmToken(request: FCMRequest): FCMResponse {
        return apiService.saveFcmToken(request)
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

    suspend fun getAllComics(): List<ComicResponse> {
        return apiService.getAllComics()
    }

    suspend fun searchComicsByTitle(titleKo: String): ComicSearchResponse {
        return apiService.searchComicsByTitle(titleKo)
    }

    suspend fun userRecommendComics(userId: Long): List<Recommendation> {
        return apiService.userRecommendComics(userId)
    }

    suspend fun todayRecommendComics(): List<Recommendation> {
        return apiService.todayRecommendComics()
    }

}