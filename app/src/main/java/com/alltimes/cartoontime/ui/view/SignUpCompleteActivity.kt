package com.alltimes.cartoontime.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alltimes.cartoontime.ui.screen.BootScreen
import com.alltimes.cartoontime.ui.screen.SignUpCompleteScreen
import com.alltimes.cartoontime.ui.viewmodel.SignUpCompleteViewModel
import com.alltimes.cartoontime.utils.NavigationHelper

class SignUpCompleteActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: SignUpCompleteViewModel = SignUpCompleteViewModel(this)

        setContent {

            SignUpCompleteScreen(viewModel)
        }

        viewModel.navigationTo.observe(this) { navigationTo ->
            navigationTo?.activityType?.let { activityType ->
                NavigationHelper.navigate(this, activityType)
            }
        }
    }
}
