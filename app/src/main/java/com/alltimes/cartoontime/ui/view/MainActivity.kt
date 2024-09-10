package com.alltimes.cartoontime.ui.view

// # Added Imports
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.main.BootScreen
import com.alltimes.cartoontime.ui.screen.main.MainScreen
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import com.alltimes.cartoontime.utils.NavigationHelper
import com.alltimes.cartoontime.utils.PermissionsHelper

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = MainViewModel(this) // ViewModel 생성

        // 권한 요청 부분을 PermissionsHelper로 처리
        if (!PermissionsHelper.hasAllPermissions(this)) {
            PermissionsHelper.requestPermissions(this)
        }

        setContent {
            navController = rememberNavController() // 전역 변수에 저장

            NavHost(navController as NavHostController, startDestination = getStartDestination()) {
                composable("bootscreen") { BootScreen(viewModel = viewModel) }
                composable("mainscreen") { MainScreen(viewModel = viewModel) }
            }
        }

        // ViewModel에서 Activity 전환 요청 처리
        viewModel.activityNavigationTo.observe(this) { navigationTo ->
            navigationTo?.activityType?.let { activityType ->
                NavigationHelper.navigate(this, activityType)
            }
        }

        // ViewModel에서 Screen 전환 요청 처리
        viewModel.screenNavigationTo.observe(this) { navigationTo ->
            navigationTo?.screenType?.let { screenType ->
                navigateToScreen(screenType)
            }
        }
    }

    // 현재 상태에 따라 올바른 시작 목적지를 반환하는 함수
    private fun getStartDestination(): String {
        // Intent로부터 데이터를 읽어오기
        val wasSignedUp = intent.getBooleanExtra("MAIN", false)
        return if (wasSignedUp) {
            "mainscreen" // 회원가입 완료 후 메인 스크린으로 시작
        } else {
            "bootscreen" // 기본적으로 부트 스크린
        }
    }

    // 스크린 전환을 처리하는 함수로 분리하여 처리
    private fun navigateToScreen(screenType: ScreenType) {
        val route = when (screenType) {
            ScreenType.BOOT -> "bootscreen"
            ScreenType.MAIN -> "mainscreen"
            else -> return
        }
        navController.navigate(route)
    }
}