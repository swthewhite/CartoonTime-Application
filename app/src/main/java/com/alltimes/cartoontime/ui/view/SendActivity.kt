package com.alltimes.cartoontime.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.moneytransaction.send.SendConfirmScreen
import com.alltimes.cartoontime.ui.screen.moneytransaction.send.SendDescriptionScreen
import com.alltimes.cartoontime.ui.screen.moneytransaction.send.SendLoadingScreen
import com.alltimes.cartoontime.ui.screen.moneytransaction.send.SendPartnerCheckScreen
import com.alltimes.cartoontime.ui.screen.moneytransaction.send.SendPasswordInputScreen
import com.alltimes.cartoontime.ui.screen.moneytransaction.send.SendPointInputScreen
import com.alltimes.cartoontime.ui.viewmodel.SendViewModel
import com.alltimes.cartoontime.utils.NavigationHelper

class SendActivity : ComponentActivity() {

    private lateinit var navController: NavController
    private lateinit var viewModel: SendViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = SendViewModel(this)

        setContent {
            navController = rememberNavController() // 전역 변수에 저장

            NavHost(navController as NavHostController, startDestination = "sendPointInputScreen") {
                composable("sendPointInputScreen") { SendPointInputScreen(viewModel = viewModel) }
                composable("sendPasswordInputScreen") { SendPasswordInputScreen(viewModel = viewModel) }
                composable("sendDescriptionScreen") { SendDescriptionScreen(viewModel = viewModel) }
                composable("sendLoadingScreen") { SendLoadingScreen(viewModel = viewModel) }
                composable("sendConfirmScreen") { SendConfirmScreen(viewModel = viewModel) }
                composable("sendPartnerCheckScreen") { SendPartnerCheckScreen(viewModel = viewModel) }
            }
        }


        // ViewModel에서 Activity 전환 요청 처리
        viewModel.activityNavigationTo.observe(this) { navigationTo ->
            navigationTo?.activityType?.let { activityType ->
                if (activityType == ActivityType.MAIN) {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("MAIN", true) // 데이터 전달
                    }
                    startActivity(intent)
                    finish()
                } else {
                    NavigationHelper.navigate(this, activityType)
                }
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
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        val route = when (screenType) {
            ScreenType.SENDPOINTINPUT -> "sendPointInputScreen"
            ScreenType.SENDPASSWORDINPUT -> "sendPasswordInputScreen"
            ScreenType.SENDDESCRIPTION -> "sendDescriptionScreen"
            ScreenType.SENDLOADING -> "sendLoadingScreen"
            ScreenType.SENDCONFIRM -> "sendConfirmScreen"
            ScreenType.SENDPARTNERCHECK -> "sendPartnerCheckScreen"
            else -> return
        }

        // 현재 화면과 다를 때 화면 전환
        if (currentRoute != route) {
            navController.navigate(route) {
                // 모든 과정에서
                popUpTo(route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}

