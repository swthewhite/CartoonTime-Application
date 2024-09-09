package com.alltimes.cartoontime.ui.view

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Observer
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.ui.screen.PasswordSettingScreen
import com.alltimes.cartoontime.ui.viewmodel.PasswordSettingViewModel
import com.alltimes.cartoontime.utils.NavigationHelper

class PasswordSettingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = PasswordSettingViewModel(this)

        setContent {

            // ViewModel을 전달하여 BootScreen과 연결
            PasswordSettingScreen(viewModel = viewModel)

        }

        // navigationTo를 관찰해서 Activity 전환 처리
        viewModel.navigationTo.observe(this) { navigationTo ->
            navigationTo?.activityType?.let { activityType ->
                NavigationHelper.navigate(this, activityType)
            }
        }
    }

}
