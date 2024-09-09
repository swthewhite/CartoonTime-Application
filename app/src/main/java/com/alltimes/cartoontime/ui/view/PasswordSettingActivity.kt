package com.alltimes.cartoontime.ui.view

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Observer
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.screen.PasswordSettingScreen
import com.alltimes.cartoontime.ui.viewmodel.PasswordSettingViewModel

class PasswordSettingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = PasswordSettingViewModel() // ViewModel 생성

        setContent {
            // ViewModel을 전달하여 BootScreen과 연결
            PasswordSettingScreen(viewModel = viewModel)

        }
    }

}