package com.alltimes.cartoontime.ui.view

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Observer
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.screen.SignUpScreen
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = SignUpViewModel() // ViewModel 생성

        setContent {
            // ViewModel을 전달하여 BootScreen과 연결
            SignUpScreen(viewModel = viewModel)

            viewModel.navigateToFinish.observe(this, Observer { shouldFinish ->
                if (shouldFinish) {
                    finish()
                }
            })


        }
    }
}
