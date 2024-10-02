package com.alltimes.cartoontime.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.boot.BootScreen
import com.alltimes.cartoontime.ui.screen.boot.LoginScreen
import com.alltimes.cartoontime.ui.viewmodel.BootViewModel
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel
import com.alltimes.cartoontime.utils.NavigationHelper


class BootActivity : ComponentActivity() {

    private lateinit var navController: NavController
    private lateinit var viewModel: BootViewModel

    private lateinit var fingerprintLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // ViewModelProvider를 사용하여 ViewModel 초기화
        viewModel = BootViewModel(application, this)


        // fingerprintLauncher 초기화
        // 지문인식을 먼저 진행
        fingerprintLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // 지문인식 성공이면 mainActivity로 이동
                    viewModel.authenticationSuccess()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // 지문인식 취소시 로그인 페이지 활성화 여기서 안해도 됨
                }
            }

        // 화면 초기화
        setContent {
            navController = rememberNavController()

            NavHost(
                navController = navController as NavHostController,
                startDestination = "bootscreen"
            ) {
                composable("bootscreen") { BootScreen(viewModel = viewModel) }
                composable("loginscreen") { LoginScreen(viewModel = viewModel) }
            }

            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val userPassword = sharedPreferences.getString("password", "")

            if (userPassword.isNullOrEmpty()) {
                // 화면에 BootScreen을 표시
                BootScreen(viewModel)
            } else {
                startFingerprintActivity()
                LoginScreen(viewModel)
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

    private fun startFingerprintActivity() {
        val intent = Intent(this, FingerprintActivity::class.java)
        fingerprintLauncher.launch(intent)
    }

    // 스크린 전환을 처리하는 함수로 분리하여 처리
    private fun navigateToScreen(screenType: ScreenType) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        val route = when (screenType) {
            ScreenType.MAIN -> "mainscreen"
            else -> return
        }

        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}