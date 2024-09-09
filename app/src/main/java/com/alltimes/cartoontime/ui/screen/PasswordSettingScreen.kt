package com.alltimes.cartoontime.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.ui.viewmodel.PasswordSettingViewModel

@Composable
fun PasswordSettingScreen(viewModel: PasswordSettingViewModel){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 간편 비밀번호 설정 타이틀
        Text(
            text = "간편 비밀번호 설정",
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
        )

        // 사용할 비밀번호를 입력하세요 설명

        // 비밀번호 6자리

        // 0 ~ 9 , 삭제 버튼
    }

}