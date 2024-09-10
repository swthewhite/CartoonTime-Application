package com.alltimes.cartoontime.ui.screen.main

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.viewmodel.BootViewModel

@Composable
fun LoginScreen(viewModel: BootViewModel) {

    val password by viewModel.password.collectAsState()

    val btnSpace = 80.dp
    val imgSize = 40.dp
    val imgSpace = 10.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 간편 비밀번호 설정 타이틀
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp), // 상단 여백 추가
            horizontalAlignment = Alignment.CenterHorizontally, // 텍스트를 가운데 정렬
            verticalArrangement = Arrangement.Center // 세로 중앙 정렬
        ) {
            Text(
                text = "간편 비밀번호",
                fontSize = 36.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "비밀번호를 입력하세요",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.padding(40.dp))

        // 비밀번호 6자리
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp, end = 50.dp), // 좌우 패딩 추가
            horizontalArrangement = Arrangement.Center
        ){
            // 첫 번째 비밀번호

            println(password + " : " + password.length)

            // 1번
            Image(
                if (password.length >= 1) painterResource(id = R.drawable.ic_filled_circle)
                else painterResource(id = R.drawable.ic_empty_circle),
                contentDescription = "password1",
                modifier = Modifier
                    .size(imgSize)
                    .padding(end = imgSpace)
                    .padding(start = imgSpace)
            )
            // 2번
            Image(
                if (password.length >= 2) painterResource(id = R.drawable.ic_filled_circle)
                else painterResource(id = R.drawable.ic_empty_circle),
                contentDescription = "password2",
                modifier = Modifier
                    .size(imgSize)
                    .padding(end = imgSpace)
                    .padding(start = imgSpace)
            )
            // 3번
            Image(
                if (password.length >= 3) painterResource(id = R.drawable.ic_filled_circle)
                else painterResource(id = R.drawable.ic_empty_circle),
                contentDescription = "password3",
                modifier = Modifier
                    .size(imgSize)
                    .padding(end = imgSpace)
                    .padding(start = imgSpace)
            )
            // 4번
            Image(
                if (password.length >= 4) painterResource(id = R.drawable.ic_filled_circle)
                else painterResource(id = R.drawable.ic_empty_circle),
                contentDescription = "password4",
                modifier = Modifier
                    .size(imgSize)
                    .padding(end = imgSpace)
                    .padding(start = imgSpace)
            )
            // 5번
            Image(
                if (password.length >= 5) painterResource(id = R.drawable.ic_filled_circle)
                else painterResource(id = R.drawable.ic_empty_circle),
                contentDescription = "password5",
                modifier = Modifier
                    .size(imgSize)
                    .padding(end = imgSpace)
                    .padding(start = imgSpace)
            )
            // 6번
            Image(
                if (password.length >= 6) painterResource(id = R.drawable.ic_filled_circle)
                else painterResource(id = R.drawable.ic_empty_circle),
                contentDescription = "password6",
                modifier = Modifier
                    .size(imgSize)
                    .padding(end = imgSpace)
                    .padding(start = imgSpace)
            )
        }

        Spacer(modifier = Modifier.padding(90.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 0 ~ 9 , 삭제 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth() // Row가 화면 너비를 꽉 채우도록 설정
            ) {
                // 1 ~ 3
                Button(
                    onClick = { viewModel.onClickedButton(1) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace) // 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "1",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }

                Button(
                    onClick = { viewModel.onClickedButton(2) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace) // 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "2",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }

                Button(
                    onClick = { viewModel.onClickedButton(3) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace) // 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "3",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth() // Row가 화면 너비를 꽉 채우도록 설정
            ) {
                // 4 ~ 6
                Button(
                    onClick = { viewModel.onClickedButton(4) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)// 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "4",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }

                Button(
                    onClick = { viewModel.onClickedButton(5) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)// 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "5",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }

                Button(
                    onClick = { viewModel.onClickedButton(6) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)// 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "6",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth() // Row가 화면 너비를 꽉 채우도록 설정
            ) {
                // 7 ~ 9
                Button(
                    onClick = { viewModel.onClickedButton(7) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)// 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "7",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }

                Button(
                    onClick = { viewModel.onClickedButton(8) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)// 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "8",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }

                Button(
                    onClick = { viewModel.onClickedButton(9) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)// 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "9",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth() // Row가 화면 너비를 꽉 채우도록 설정
            ) {
                Button(
                    onClick = {  },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)// 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = " ",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }

                Button(
                    onClick = { viewModel.onClickedButton(0) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)// 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "0",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }

                Button(
                    onClick = { viewModel.onClickedButton(-1) },
                    shape = RoundedCornerShape(0.dp), // 네모난 모양으로 설정
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)// 버튼이 균등하게 배치되도록 설정
                ) {
                    Text(
                        text = "<-",
                        color = Color(0xFF000000),
                        fontSize = 40.sp
                    )
                }
            }
        }
    }
}