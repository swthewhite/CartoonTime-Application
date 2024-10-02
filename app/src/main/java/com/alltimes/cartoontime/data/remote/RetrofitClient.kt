package com.alltimes.cartoontime.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://j11a507.p.ssafy.io/"

    private lateinit var retrofit: Retrofit

    fun getRetrofitInstance(context: Context): Retrofit {
        if (!::retrofit.isInitialized) {
            // OkHttpClient에 AuthInterceptor 추가
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }

    val apiService: ApiService
        get() = retrofit.create(ApiService::class.java)
}



//object RetrofitClient {
//    private const val BASE_URL = "https://j11a507.p.ssafy.io/"
//
//    private val retrofit: Retrofit = Retrofit.Builder()
//        .baseUrl(BASE_URL)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    val apiService: ApiService = retrofit.create(ApiService::class.java)
//}