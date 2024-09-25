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
    CONFIRM({ navController -> navController.navigate("confirmScreen") }),

    // 기타
    SEND({ navController -> navController.navigate("sendScreen") }),
    RECEIVE({ navController -> navController.navigate("receiveScreen") }),

    // 송금
    SENDPOINTINPUT({ navController -> navController.navigate("sendPointInputScreen") }),
    SENDPASSWORDINPUT({ navController -> navController.navigate("sendPasswordInputScreen") }),
    SENDLOADING({ navController -> navController.navigate("sendLoadingScreen") }),
    SENDDESCRIPTION({ navController -> navController.navigate("sendDescriptionScreen") }),
    SENDCONFIRM({ navController -> navController.navigate("sendConfirmScreen") }),
    SENDPARTNERCHECK({ navController -> navController.navigate("sendPartnerCheckScreen") }),

    // 입금
    RECEIVELOADING({ navController -> navController.navigate("receiveLoadingScreen") }),
    RECEIVECONFIRM({ navController -> navController.navigate("receiveConfirmScreen") }),
    RECEIVEDESCRIPTION({ navController -> navController.navigate("receiveDescriptionScreen") }),
    RECEIVEPARTNERREADY({ navController -> navController.navigate("receivePartnerReadyScreen") }),

    // 충전
    CHARGEPOINTINPUT({ navController -> navController.navigate("chargePointInputScreen") }),
    CHARGEPASSWORDINPUT({ navController -> navController.navigate("chargePasswordInputScreen") }),
    CHARGECONFIRM({ navController -> navController.navigate("chargeConfirmScreen") }),

    // 회원가입
    SIGNUP({ navController -> navController.navigate("signUpScreen") }),
    SIGNUPCOMPLETE({ navController -> navController.navigate("signUpCompleteScreen") }),
    PASSWORDSETTING({ navController -> navController.navigate("passwordSettingScreen") }),
    NAVERLOGIN({ navController -> navController.navigate("naverLoginScreen") }),
}