package com.alltimes.cartoontime.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alltimes.cartoontime.ui.screen.NaverLoginScreen
import com.alltimes.cartoontime.ui.viewmodel.NaverLoginViewModel
import com.alltimes.cartoontime.utils.NavigationHelper

class NaverLoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = NaverLoginViewModel(this)

        setContent {

            // ViewModel을 전달하여 BootScreen과 연결
            NaverLoginScreen(viewModel = viewModel)

        }

        // navigationTo를 관찰해서 Activity 전환 처리
        viewModel.navigationTo.observe(this) { navigationTo ->
            navigationTo?.activityType?.let { activityType ->
                NavigationHelper.navigate(this, activityType)
            }
        }
    }

}