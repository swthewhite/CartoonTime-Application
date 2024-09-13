package com.alltimes.cartoontime.data.model.ui

import androidx.navigation.NavController

data class ScreenNavigationTo(val screenType: ScreenType)

// Enum 클래스에서 Intent 정보를 관리하도록 수정
enum class ScreenType(val navigateTo: (NavController) -> Unit) {
    FINISH({ null }),
    // 부트
    BOOT({ navController -> navController.navigate("bootScreen") }),
    LOGIN({ navController -> navController.navigate("loginScreen") }),
    
    // 메인
    MAIN({ navController -> navController.navigate("mainScreen") }),
    BOOKRECOMMEND({ navController -> navController.navigate("bookRecommendScreen") }),
    BOOKDETAIL({ navController -> navController.navigate("bookDetailScreen") }),
    
    // 송금 & 입금
    SEND({ navController -> navController.navigate("sendScreen") }),
    PASSWORDINPUT({ navController -> navController.navigate("passwordInputScreen") }),
    POINTINPUT({ navController -> navController.navigate("pointInputScreen") }),
    DESCRIPTION({ navController -> navController.navigate("descriptionScreen") }),
    LOADING({ navController -> navController.navigate("loadingScreen") }),
    CONFIRM({ navController -> navController.navigate("confirmScreen") }),
    RECEIVE({ navController -> navController.navigate("receiveScreen") }),
    
    // 회원가입
    SIGNUP({ navController -> navController.navigate("signUpScreen") }),
    SIGNUPCOMPLETE({ navController -> navController.navigate("signUpCompleteScreen") }),
    PASSWORDSETTING({ navController -> navController.navigate("passwordSettingScreen") }),
    NAVERLOGIN({ navController -> navController.navigate("naverLoginScreen") }),
}