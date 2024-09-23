package com.alltimes.cartoontime.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("ct/auth")
    suspend fun requestAuthCode(@Body request: AuthRequest): AuthResponse

    @POST("ct/verify-auth")
    suspend fun verifyAuthCode(@Body request: VerifyAuthRequest): VerifyAuthResponse

    @POST("ct/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): SignResponse

    @POST("ct/sign-in")
    suspend fun signIn(@Body request: SignInRequest): SignResponse

    @GET("ct/user/{user_id}")
    suspend fun getUserInfo(@Path("user_id") userId: Long): UserResponse

    @POST("ct/auth/naver")
    suspend fun naverAuth(@Body request: NaverAuthRequest): NavertAuthResponse

    @POST("ct/charge")
    suspend fun charge(@Body request: ChargeRequest): ChargeResponse

    @POST("ct/transfer")
    suspend fun transfer(@Body request: TransferRequest): TransferResponse

    @POST("ct/pay")
    suspend fun pay(@Body request: PayRequest): PayResponse

    @GET("ct/entries/all/{user_id}")
    suspend fun getEntryLog(@Path("user_id") userId: Long): EntryLogResponse
}

// 내 지갑 정보
data class AccountData(
    val userId: Long,
    val currentMoney: Long,
)

// 인증번호 요청 /ct/auth
data class AuthRequest(
    val phoneNumber: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: Any? // response data is null in this case
)

// 인증 번호 확인 /ct/verify-auth
data class VerifyAuthRequest(
    val phoneNumber: String,
    val authCode: String
)

data class VerifyAuthResponse(
    val success: Boolean,
    val message: String,
    val data: VerifyAuthData?,
    val error: Any?
)

data class VerifyAuthData(
    val user: UserID?
)

data class UserID(
    val id: Long
)

// 회원가입 /ct/sign-up
data class SignUpRequest(
    val phoneNumber: String,
    val name: String,
)

data class SignInRequest(
    val phoneNumber: String,
)

// 회원가입, 로그인 응답
// /ct/sign-up, /ct/sign-in
data class SignResponse(
    val success: Boolean,
    val message: String,
    val data: SignResponseData?,
    val error: Any?
)

data class SignResponseData(
    val user: User,
    val jwtToken: JwtToken
)

data class User(
    val id: Long,
    val username: String,
    val name: String
)

data class JwtToken(
    val grantType: String,
    val accessToken: String,
    val refreshToken: String
)

// 유저 정보 조회 /ct/user/{user_id}
data class UserResponse(
    val id: Long,
    val username: String,
    val name: String,
    val currentMoney: Long,
    val roles: List<String>,
    val mainAccount: MainAccount
)

data class MainAccount(
    val id: Long,
    val accountNumber: String,
    val bankName: String,
    val userId: Long
)

// 네이버 아이디 비밀번호 받기 /ct/auth/naver
data class NaverAuthRequest(
    val userId: String,
    val naverId: String,
    val naverPassword: String
)

data class NavertAuthResponse(
    val success: Boolean,
    val message: String,
    val data: Any?
)

// 포인트 충전 /ct/charge
data class ChargeRequest(
    val userId: Long,
    val amount: Long
)

data class ChargeResponse(
    val success: Boolean,
    val message: String,
    val data: AccountData?,
    val error: Any?
)

// 포인트 송금 /ct/transfer
data class TransferRequest(
    val fromUserId: Long,
    val toUserId: Long,
    val amount: Long
)

data class TransferResponse(
    val success: Boolean,
    val message: String,
    val data: TransferData?,
    val error: Any?
)

data class TransferData(
    val from: AccountData,
    val to: AccountData
)

// 포인트 결제 /ct/pay
data class PayRequest(
    val userId: Long,
    val amount: Long
)

data class PayResponse(
    val success: Boolean,
    val message: String,
    val data: AccountData,
    val error: Any?
)

// 입퇴실 조회 /ct/entries/all/{user_id}
data class EntryLogRequest(
    val userId: Long
)

data class EntryLogResponse(
    val success: Boolean,
    val message: String,
    val data: List<EntryLog>,
    val error: Any?
)

data class EntryLog(
    val userId: Long,
    val entryDate: String,
    val exitDate: String?,
    val fee: Long,
)