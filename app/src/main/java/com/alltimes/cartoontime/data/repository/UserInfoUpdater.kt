package com.alltimes.cartoontime.data.repository

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserInfoUpdater(
    private val repository: UserRepository,
    private val sharedPreferences: SharedPreferences
) {

    val editor = sharedPreferences.edit()

    fun updateUserInfo(userId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            // API 호출
            val response = try {
                repository.getUserInfo(userId)
            } catch (e: Exception) {
                // 에러처리
                null
            }

            // 응답 처리
            if (response?.success == true) {
                editor.putLong("balance", response.data?.currentMoney!!)
                editor.putLong("userId", response.data?.id!!)
                editor.putString("userName", response.data?.username!!)
                editor.putString("name", response.data?.name!!)

                editor.apply()
            }

        }
    }
}