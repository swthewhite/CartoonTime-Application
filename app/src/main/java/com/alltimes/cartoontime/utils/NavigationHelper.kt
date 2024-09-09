package com.alltimes.cartoontime.utils

import android.app.Activity
import android.content.Intent
import com.alltimes.cartoontime.data.model.ActivityType

object NavigationHelper {

    fun navigate(activity: Activity, activityType: ActivityType) {
        val intent = activityType.intentCreator(activity)
        if (activityType == ActivityType.FINISH) {
            activity.finish() // 현재 Activity 종료
        } else if (intent != null) {
            activity.startActivity(intent) // Intent가 null이 아닐 때만 Activity 시작
        }
    }
}
