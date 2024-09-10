package com.alltimes.cartoontime.ui.screen.signup

import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel

@Composable
fun SignUpCompleteScreen(viewModel: SignUpViewModel) {

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_cartoontime),
            contentDescription = "logo_cartoontime",
            modifier = Modifier
                .width(250.dp)
                .height(100.dp)
                .padding(top = 70.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(220.dp))

        Text(
            text = "가입이 완료 되었어요.",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,  // 가운데 정렬
            modifier = Modifier.fillMaxWidth()  // 텍
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "지금 바로 카툰타임을 \n 시작해 보세요!",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,  // 가운데 정렬
            modifier = Modifier.fillMaxWidth()  // 텍
        )

        Spacer(modifier = Modifier.height(300.dp))

        Button(
            onClick = { viewModel.onClick() },
            shape = RoundedCornerShape(15),
            colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
            modifier = Modifier
                .width(350.dp)
                .height(60.dp)
                .padding(8.dp)
        ) {
            Text(
                text = "시작하기",
                fontSize = 16.sp,
                color = Color(0xFF606060),
            )
        }
    }
}