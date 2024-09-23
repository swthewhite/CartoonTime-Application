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
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel

@Composable
fun SignUpCompleteScreen(viewModel: SignUpViewModel) {

    val isSignup = viewModel.isSignUp

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (logo, message, subMessage, button) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.logo_cartoontime),
            contentDescription = "logo_cartoontime",
            modifier = Modifier
                .width(250.dp)
                .height(100.dp)
                .constrainAs(logo) {
                    top.linkTo(parent.top, margin = 70.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            contentScale = ContentScale.Fit
        )

        Text(
            text = if (isSignup) "회원가입이 완료 되었어요." else "로그인이 완료 되었어요.",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(message) {
                    top.linkTo(logo.bottom, margin = 180.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
        )

        Text(
            text = "지금 바로 카툰타임을 \n 시작해 보세요!",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(subMessage) {
                    top.linkTo(message.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
        )

        Button(
            onClick = { viewModel.goActivity(ActivityType.MAIN) },
            shape = RoundedCornerShape(15),
            colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
            modifier = Modifier
                .width(350.dp)
                .height(60.dp)
                .constrainAs(button) {
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = "시작하기",
                fontSize = 16.sp,
                color = Color(0xFF606060),
            )
        }
    }
}