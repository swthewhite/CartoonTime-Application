package com.alltimes.cartoontime.ui.screen.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel
import androidx.constraintlayout.compose.ConstraintLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaverLoginScreen(viewModel: SignUpViewModel) {

    val name = viewModel.userName
    val naverID by viewModel.naverID.collectAsState()
    val naverPassword by viewModel.naverPassword.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (logoNaver, logoCartoonTime, welcomeText, infoText1, infoText2, idField, passwordField, loginButton, footerText) = createRefs()


        Image(
            painter = painterResource(id = R.drawable.logo_naver),
            contentDescription = "logo_naver",
            modifier = Modifier
                .size(80.dp)
                .constrainAs(logoNaver) {
                    top.linkTo(parent.top, margin = 100.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                }
        )

        Image(
            painter = painterResource(id = R.drawable.logo_cartoontime),
            contentDescription = "logo_cartoontime",
            modifier = Modifier
                .width(250.dp)
                .height(100.dp)
                .constrainAs(logoCartoonTime) {
                    start.linkTo(logoNaver.end, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                    centerVerticallyTo(logoNaver) // Center vertically to the first logo
                },
            contentScale = ContentScale.Fit
        )

        Text(
            text = "${name}님 반가워요!",
            fontSize = 24.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .constrainAs(welcomeText) {
                    top.linkTo(logoCartoonTime.bottom, margin = 40.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "만화 추천 서비스 이용을 위해",
            fontSize = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .constrainAs(infoText1) {
                    top.linkTo(welcomeText.bottom, margin = 20.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "네이버 로그인을 진행해주세요.",
            fontSize = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .constrainAs(infoText2) {
                    top.linkTo(infoText1.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        // 네이버 로그인 필드 및 버튼
        TextField(
            value = naverID,
            onValueChange = { viewModel.onNaverIDChanged(it) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Black,
            ),
            modifier = Modifier
                .width(350.dp)
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(2.dp)
                .constrainAs(idField) {
                    top.linkTo(infoText2.bottom, margin = 50.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                },
            label = {
                Text(text = "아이디", color = Color.Black)
            }
        )

        TextField(
            value = naverPassword,
            onValueChange = { viewModel.onNaverPasswordChanged(it) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Black,
            ),
            modifier = Modifier
                .width(350.dp)
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(2.dp)
                .constrainAs(passwordField) {
                    top.linkTo(idField.bottom, margin = 1.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                },
            label = {
                Text(text = "비밀번호", color = Color.Black)
            }
        )

        Box(
            modifier = Modifier
                .width(200.dp)
                .height(70.dp)
                .background(Color.Transparent, RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { viewModel.onLogin() }
                )
                .constrainAs(loginButton) {
                    top.linkTo(passwordField.bottom, margin = 50.dp)
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                },
        ) {
            Image(
                painter = painterResource(id = R.drawable.btn_naver_login),
                contentDescription = "Naver Login",
                modifier = Modifier
                    .width(200.dp)
                    .height(70.dp)
            )
        }

        Text(
            text = "해당 사용자 정보는 만화추천서비스 \n 이외에는 사용되지않습니다.",
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(footerText) {
                    bottom.linkTo(parent.bottom, margin = 10.dp)
                }
        )
    }
}