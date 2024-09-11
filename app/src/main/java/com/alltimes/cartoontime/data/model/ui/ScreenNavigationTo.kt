package com.alltimes.cartoontime.data.model.ui

import androidx.navigation.NavController

data class ScreenNavigationTo(val screenType: ScreenType)

// Enum 클래스에서 Intent 정보를 관리하도록 수정
enum class ScreenType(val navigateTo: (NavController) -> Unit) {
    FINISH({ null }),
    BOOT({ navController -> navController.navigate("bootScreen") }),
    MAIN({ navController -> navController.navigate("mainScreen") }),
    BOOKRECOMMEND({ navController -> navController.navigate("bookRecommendScreen") }),
    BOOKDETAIL({ navController -> navController.navigate("bookDetailScreen") }),
    SEND({ navController -> navController.navigate("sendScreen") }),
    RECEIVE({ navController -> navController.navigate("receiveScreen") }),
    SIGNUP({ navController -> navController.navigate("signUpScreen") }),
    SIGNUPCOMPLETE({ navController -> navController.navigate("signUpCompleteScreen") }),
    PASSWORDSETTING({ navController -> navController.navigate("passwordSettingScreen") }),
    NAVERLOGIN({ navController -> navController.navigate("naverLoginScreen") }),
    LOGIN({ navController -> navController.navigate("loginScreen") }),
}