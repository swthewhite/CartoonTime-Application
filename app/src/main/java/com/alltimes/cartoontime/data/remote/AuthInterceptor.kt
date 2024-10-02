package com.alltimes.cartoontime.data.remote

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        // SharedPreferences에서 JWT 토큰 가져오기
        val accessToken = sharedPreferences.getString("accessToken", null)

        // 요청을 빌드
        val requestBuilder = chain.request().newBuilder()
        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}