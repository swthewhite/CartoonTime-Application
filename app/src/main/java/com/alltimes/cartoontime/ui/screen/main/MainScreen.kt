package com.alltimes.cartoontime.ui.screen.main


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import com.alltimes.cartoontime.ui.screen.composable.ApproachAnimate
import com.alltimes.cartoontime.ui.screen.composable.PhoneReverseAnimate
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

@Composable
fun MainScreen(viewModel: MainViewModel) {

    // 애니메이션 상태를 기억하는 상태 변수
    var currentAnimation by remember { mutableStateOf(1) }

    val name = viewModel.userName
    val balance by viewModel.balance.collectAsState()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE)),
        horizontalAlignment = Alignment.CenterHorizontally, // Column 내 모든 요소를 왼쪽 정렬
        verticalArrangement = Arrangement.Top// 중앙 정렬
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_cartoontime),
            contentDescription = "Main Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(300.dp)
                .height(120.dp)
                .padding(top = 20.dp)
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_line),
                contentDescription = "Line Icon",
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp)
                    .padding(end = 10.dp)
            )

            Text(
                text = "만화와 소중했던 그 시간들",
            )

            Image(
                painter = painterResource(id = R.drawable.ic_line),
                contentDescription = "Line Icon",
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp)
                    .padding(start = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "$name" + "님 환영합니다.",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 전체 지갑 형태의 컨테이너
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(Color(0xFFF9B912), RoundedCornerShape(16.dp))
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 첫 번째 Column
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_wallet), // 아이콘 이미지
                        contentDescription = "Icon",
                        modifier = Modifier
                            .size(40.dp)
                    )
                    Text(
                        text = "내 지갑",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$balance" + "  Points",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.onSendButtonClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A911)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "송금하기",
                            color = Color.Black
                        )
                    }
                    Button(
                        onClick = { viewModel.onReceiveButtonClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A911)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "송금받기",
                            color = Color.Black
                        )
                    }
                    Button(
                        onClick = {  },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A911)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "충전",
                            color = Color.Black
                        )
                    }
                }

            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 5.dp)
        ) {
            AnimateSequence { animationId ->
                currentAnimation = animationId
            }
        }

        Text(
            text = "$state",
            fontSize = 20.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (state == "입실 전") {
            Text(
                text = if (currentAnimation == 1) {
                    "핸드폰을 키오스크 가까이에 갖다 대주세요."
                } else {
                    "손 안의 핸드폰을 뒤집어서 입실을 진행해주세요."
                },
                fontSize = 15.sp,
            )
        }
        else if (state == "입실 완료") {
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.onSendButtonClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "추천만화 확인하기",
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun AnimateSequence(onAnimationChange: (Int) -> Unit) {
    var isFirstAnimation by remember { mutableStateOf(true) } // 첫 번째 애니메이션인지 여부

    // 반복적으로 애니메이션을 전환하는 LaunchedEffect
    LaunchedEffect(Unit) {
        while (true) {
            onAnimationChange(1)
            delay(2500) // 첫 번째 애니메이션이 3초 동안 진행
            isFirstAnimation = false // 두 번째 애니메이션으로 전환
            onAnimationChange(2)
            delay(4000) // 두 번째 애니메이션이 3초 동안 진행
            isFirstAnimation = true // 다시 첫 번째 애니메이션으로 전환
        }
    }

    if (isFirstAnimation) {
        ApproachAnimate() // 첫 번째 애니메이션
    } else {
        PhoneReverseAnimate() // 두 번째 애니메이션
    }
}