package com.alltimes.cartoontime.data.remote

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://j11a507.p.ssafy.io/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

//object RetrofitClient {
//    private const val BASE_URL = "https://j11a507.p.ssafy.io/"
//
//    // ApplicationContext를 통해 SharedPreferences를 가져올 수 있도록 설정
//    private lateinit var sharedPreferences: SharedPreferences
//
//    fun initialize(context: Context) {
//        sharedPreferences = context.getSharedPreferences("YourPrefsName", Context.MODE_PRIVATE)
//    }
//
//    private val retrofit: Retrofit by lazy {
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(AuthInterceptor {
//                sharedPreferences.getString("accessToken", null)
//            })
//            .build()
//
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    val apiService: ApiService = retrofit.create(ApiService::class.java)
//}