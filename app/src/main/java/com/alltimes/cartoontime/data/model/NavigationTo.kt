package com.alltimes.cartoontime.data.model

import android.content.Context
import android.content.Intent

data class NavigationTo(val activityType: ActivityType)

// Enum 클래스에서 Intent 정보를 관리하도록 수정
enum class ActivityType(val intentCreator: (Context) -> Intent?) {
    FINISH({ null }),
    MAIN({context-> Intent(context, com.alltimes.cartoontime.ui.view.MainActivity::class.java)}),
    SEND({ context -> Intent(context, com.alltimes.cartoontime.ui.view.SendActivity::class.java) }),
    RECEIVE({ context -> Intent(context, com.alltimes.cartoontime.ui.view.ReceiveActivity::class.java) }),
    SIGNUP({ context -> Intent(context, com.alltimes.cartoontime.ui.view.SignUpActivity::class.java) }),
    SIGNUPCOMPLETE({ context -> Intent(context, com.alltimes.cartoontime.ui.view.SignUpCompleteActivity::class.java) }),
    PASSWORDSETTING({ context -> Intent(context, com.alltimes.cartoontime.ui.view.PasswordSettingActivity::class.java) }),
    NAVERLOGIN({ context -> Intent(context, com.alltimes.cartoontime.ui.view.NaverLoginActivity::class.java) })
}