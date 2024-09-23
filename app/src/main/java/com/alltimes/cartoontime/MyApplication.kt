package com.alltimes.cartoontime

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.alltimes.cartoontime.data.remote.RetrofitClient

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // RetrofitClient 초기화
        RetrofitClient.initialize(this)

        println("초기화 완료")
    }
}