package com.alltimes.cartoontime.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val requestBuilder: Request.Builder = original.newBuilder()

        // JWT 토큰을 헤더에 추가
        tokenProvider()?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request: Request = requestBuilder.build()
        return chain.proceed(request)
    }
}