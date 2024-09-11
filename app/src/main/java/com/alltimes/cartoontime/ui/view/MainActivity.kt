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
import com.alltimes.cartoontime.ui.screen.main.BookDetailScreen
import com.alltimes.cartoontime.ui.screen.main.BookRecommendScreen
import com.alltimes.cartoontime.ui.screen.main.MainScreen
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import com.alltimes.cartoontime.utils.AccelerometerManager
import com.alltimes.cartoontime.utils.NavigationHelper
import com.alltimes.cartoontime.utils.PermissionsHelper
import kotlin.properties.Delegates

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel

    private lateinit var accelerometerManager: AccelerometerManager
    private var accelerometerCount by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel(this) // ViewModel 생성

        // 권한 요청 부분을 PermissionsHelper로 처리
        if (!PermissionsHelper.hasAllPermissions(this)) {
            PermissionsHelper.requestPermissions(this)
        }

        accelerometerManager = AccelerometerManager(this)
        accelerometerCount = 0

        setContent {
            navController = rememberNavController() // 전역 변수에 저장

            NavHost(navController as NavHostController, startDestination = "mainscreen") {
                composable("mainscreen") { MainScreen(viewModel = viewModel) }
                composable("bookRecommendScreen") { BookRecommendScreen(viewModel = viewModel) }
                composable("bookDetailScreen") { BookDetailScreen(viewModel = viewModel) }
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

        accelerometerManager.accelerometerData.observe(this) { data ->
            // 데이터 업데이트
            if (data.z <= -9.0) {
                // 아래를 보는 중
                accelerometerCount++
                if (accelerometerCount >= 15) viewModel.onLogin()
            } else if (data.z >= 0) {
                // 위를 보는 중
                accelerometerCount = 0
            }
        }
    }

    // 스크린 전환을 처리하는 함수로 분리하여 처리
    private fun navigateToScreen(screenType: ScreenType) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        val route = when (screenType) {
            ScreenType.MAIN -> "mainscreen"
            ScreenType.BOOKRECOMMEND -> "bookRecommendScreen"
            ScreenType.BOOKDETAIL -> "bookDetailScreen"
            else -> return
        }

        // 현재 화면이 이동하려는 화면과 다를 경우에만 화면 전환
        if (currentRoute != route) {
            navController.navigate(route) {
                if (screenType == ScreenType.MAIN) popUpTo("mainscreen") { inclusive = true }
                else if (screenType == ScreenType.BOOKDETAIL) popUpTo("bookRecommendScreen") { inclusive = false }
                launchSingleTop = true // 동일 화면 중복 쌓임 방지
            }
        }
    }
}