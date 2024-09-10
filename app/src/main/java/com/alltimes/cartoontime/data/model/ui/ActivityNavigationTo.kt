package com.alltimes.cartoontime.data.model.ui

import android.content.Context
import android.content.Intent

data class ActivityNavigationTo(val activityType: ActivityType)

// Enum 클래스에서 Intent 정보를 관리하도록 수정
enum class ActivityType(val intentCreator: (Context) -> Intent?) {
    FINISH({ null }),
    MAIN({context-> Intent(context, com.alltimes.cartoontime.ui.view.MainActivity::class.java)}),
    SEND({ context -> Intent(context, com.alltimes.cartoontime.ui.view.SendActivity::class.java) }),
    RECEIVE({ context -> Intent(context, com.alltimes.cartoontime.ui.view.ReceiveActivity::class.java) }),
    SIGNUP({ context -> Intent(context, com.alltimes.cartoontime.ui.view.SignUpActivity::class.java) }),
}