package com.alltimes.cartoontime.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.viewmodel.NaverLoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaverLoginScreen(viewModel: NaverLoginViewModel) {

    val name = "오현진"

    val naverID by viewModel.naverID.collectAsState()
    val naverPassword by viewModel.naverPassword.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE)),
        verticalArrangement = Arrangement.Top
    ) {
        // 상단 로고 및 문구
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally // 수평 중앙 정렬
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_naver),
                    contentDescription = "logo_naver",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.logo_cartoontime),
                    contentDescription = "logo_cartoontime",
                    modifier = Modifier
                        .width(250.dp)
                        .height(100.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${name}님 반가워요!",
                fontSize = 24.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = " 만화 추천 서비스 이용을 위해 \n 네이버 로그인을 진행해주세요. ",
                fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 네이버 로그인 필드 및 버튼
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        ) {
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
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(2.dp),
                label = {
                    Text(text = "아이디", color = Color.Black)
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

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
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(2.dp),
                label = {
                    Text(text = "비밀번호", color = Color.Black)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Unspecified
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.btn_naver_login),
                    contentDescription = "Naver Login",
                    modifier = Modifier
                        .width(200.dp)
                        .height(70.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(200.dp))

        // 하단 텍스트
        Text(
            text = "해당 사용자 정보는 만화추천서비스 \n 이외에는 사용되지않습니다.",
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp) // 화면 하단에서 5dp 떨어진 위치
        )
    }
}
