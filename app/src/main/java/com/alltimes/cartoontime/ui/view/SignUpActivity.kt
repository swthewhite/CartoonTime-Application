package com.alltimes.cartoontime.ui.view

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Observer
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.ui.screen.SignUpScreen
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = SignUpViewModel() // ViewModel 생성

        setContent {
            // ViewModel을 전달하여 BootScreen과 연결
            SignUpScreen(viewModel = viewModel)

        }

        // navigationTo를 관찰해서 Activity 전환 처리
        viewModel.navigationTo.observe(this) { navigationTo ->
            navigationTo?.activityType?.let { activityType ->
                navigateToActivity(activityType)
            }
        }
    }

    // Activity 전환 함수
    private fun navigateToActivity(activityType: ActivityType) {
        val intent = activityType.intentCreator(this)
        if (activityType == ActivityType.FINISH) {
            finish() // 현재 Activity 종료
        } else if (intent != null) {
            startActivity(intent) // Intent가 null이 아닐 때만 Activity 시작
        }
    }
}
