package com.alltimes.cartoontime.ui.view

import android.annotation.SuppressLint
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
import com.alltimes.cartoontime.ui.screen.moneytransaction.receive.ReceiveConfirmScreen
import com.alltimes.cartoontime.ui.screen.moneytransaction.receive.ReceiveDescriptionScreen
import com.alltimes.cartoontime.ui.screen.moneytransaction.receive.ReceiveLoadingScreen
import com.alltimes.cartoontime.ui.screen.moneytransaction.receive.ReceivePartnerReadyScreen
import com.alltimes.cartoontime.ui.viewmodel.ReceiveViewModel
import com.alltimes.cartoontime.utils.NavigationHelper

class ReceiveActivity : ComponentActivity() {

    private lateinit var navController: NavController
    private lateinit var viewModel: ReceiveViewModel

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ReceiveViewModel(this)
        viewModel.transactionBleServerStart()

        setContent {
            navController = rememberNavController() // 전역 변수에 저장

            NavHost(
                navController as NavHostController,
                startDestination = "receivePartnerReadyScreen"
            ) {
                composable("receiveDescriptionScreen") { ReceiveDescriptionScreen(viewModel = viewModel) }
                composable("receiveLoadingScreen") { ReceiveLoadingScreen(viewModel = viewModel) }
                composable("receiveConfirmScreen") { ReceiveConfirmScreen(viewModel = viewModel) }
                composable("receivePartnerReadyScreen") { ReceivePartnerReadyScreen(viewModel = viewModel) }
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

    override fun onPause() {
        super.onPause()

        viewModel.onPuaseAll()
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResumeAll()
    }

    // 스크린 전환을 처리하는 함수로 분리하여 처리
    private fun navigateToScreen(screenType: ScreenType) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        val route = when (screenType) {
            ScreenType.RECEIVEDESCRIPTION -> "receiveDescriptionScreen"
            ScreenType.RECEIVELOADING -> "receiveLoadingScreen"
            ScreenType.RECEIVECONFIRM -> "receiveConfirmScreen"
            ScreenType.RECEIVEPARTNERREADY -> "receivePartnerReadyScreen"
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
