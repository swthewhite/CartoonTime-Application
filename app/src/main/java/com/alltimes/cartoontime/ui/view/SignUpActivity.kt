package com.alltimes.cartoontime.ui.view

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.data.model.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ScreenType
import com.alltimes.cartoontime.ui.screen.NaverLoginScreen
import com.alltimes.cartoontime.ui.screen.PasswordSettingScreen
import com.alltimes.cartoontime.ui.screen.SignUpCompleteScreen
import com.alltimes.cartoontime.ui.screen.SignUpScreen
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel
import com.alltimes.cartoontime.utils.NavigationHelper

class SignUpActivity : ComponentActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = SignUpViewModel(this) // ViewModel 생성

        setContent {
            navController = rememberNavController() // 전역 변수에 저장

            NavHost(navController as NavHostController, startDestination = "signupscreen") {
                composable("signupscreen") { SignUpScreen(viewModel = viewModel) }
                composable("passwordsettingscreen") { PasswordSettingScreen(viewModel = viewModel) }
                composable("naverloginscreen") { NaverLoginScreen(viewModel = viewModel) }
                composable("signupcompletescreen") { SignUpCompleteScreen(viewModel = viewModel) }
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

    // 스크린 전환을 처리하는 함수로 분리하여 처리
    private fun navigateToScreen(screenType: ScreenType) {
        val route = when (screenType) {
            ScreenType.PASSWORDSETTING -> "passwordsettingscreen"
            ScreenType.NAVERLOGIN -> "naverloginscreen"
            ScreenType.SIGNUPCOMPLETE -> "signupcompletescreen"
            else -> return
        }
        navController.navigate(route)
    }
}