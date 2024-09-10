package com.alltimes.cartoontime.ui.view

// # Added Imports
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.ui.screen.MainScreen
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import com.alltimes.cartoontime.ui.theme.CartoonTimeTheme
import com.alltimes.cartoontime.ui.viewmodel.BootViewModel
import com.alltimes.cartoontime.utils.NavigationHelper
import com.alltimes.cartoontime.utils.PermissionsHelper

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 요청 부분을 PermissionsHelper로 처리
        if (!PermissionsHelper.hasAllPermissions(this)) {
            PermissionsHelper.requestPermissions(this)
        }

        val viewModel: MainViewModel = MainViewModel(this)

        setContent {
            CartoonTimeTheme {
                MainScreen(viewModel)
            }
        }

        // navigationTo를 관찰해서 Activity 전환 처리
        viewModel.activityNavigationTo.observe(this) { navigationTo ->
            navigationTo?.activityType?.let { activityType ->
                NavigationHelper.navigate(this, activityType)
            }
        }
    }
}
