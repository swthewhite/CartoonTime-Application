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
import com.alltimes.cartoontime.utils.PermissionsHelper

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 요청 부분을 PermissionsHelper로 처리
        if (!PermissionsHelper.hasAllPermissions(this)) {
            PermissionsHelper.requestPermissions(this)
        }

        setContent {
            CartoonTimeTheme {
                viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
                MainScreen(viewModel)
            }
        }

        // navigationTo를 관찰해서 Activity 전환 처리
        viewModel.navigationTo.observe(this) { navigationTo ->
            navigationTo?.activityType?.let { activityType ->
                navigateToActivity(activityType)
            }
        }
    }

    // Activity 전환 함수
    private fun navigateToActivity(activityType: ActivityType) {
        val intent = activityType.intentCreator(this)
        if (activityType == ActivityType.FINISH) {
            finish() // 현재 Activity 종료
        } else if (intent != null) {
            startActivity(intent) // Intent가 null이 아닐 때만 Activity 시작
        }
    }
}
