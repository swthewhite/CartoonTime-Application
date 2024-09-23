package com.alltimes.cartoontime.ui.screen.moneytransaction.send

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.composable.SendAnimation
import com.alltimes.cartoontime.ui.viewmodel.SendViewModel
import kotlinx.coroutines.delay

@Composable
fun SendLoadingScreen(viewModel: SendViewModel) {

    var dots by remember { mutableStateOf("") }

    // 점의 개수를 0.1초마다 업데이트
    LaunchedEffect(Unit) {
        while (true) {
            dots = when (dots.length) {
                5 -> ""
                else -> dots + "."
            }
            delay(500) // 0.1초마다 실행
        }
    }


    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (backButton, title, nextbtn, animationBox, description0, description1) = createRefs()

        // 뒤로가기 버튼
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .background(Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.goScreen(ScreenType.SENDPOINTINPUT)
                    }
                )
                .constrainAs(backButton) {
                    top.linkTo(parent.top, margin = 10.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back Icon",
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
            )
        }

        // 상단 타이틀
        Text(
            text = "Send Witch",
            fontSize = 30.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(backButton.top)   // 이미지의 상단에 맞추고
                bottom.linkTo(backButton.bottom)  // 이미지의 하단에 맞춤 (세로 중앙 정렬)
                start.linkTo(parent.start, margin = 10.dp)  // 홈버튼 오른쪽에 위치
                end.linkTo(parent.end, margin = 10.dp)
                width = Dimension.wrapContent
            }
        )

        Button(
            onClick = { viewModel.goScreen(ScreenType.SENDCONFIRM) },
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(nextbtn) {
                    top.linkTo(title.top)
                    bottom.linkTo(title.bottom)
                    start.linkTo(title.end, margin = 10.dp)
                }
        ) {
            Text("다음")
        }

        // 점이 변화하는 텍스트
        Text(
            text = "포인트 보내는 중 $dots",
            fontSize = 24.sp,
            modifier = Modifier
                .constrainAs(description0) {
                    bottom.linkTo(parent.bottom, margin = 200.dp)
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            color = Color.Black
        )

        // 애니메이션 뷰
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                //.background(Color.Black)
                .constrainAs(animationBox) {
                    top.linkTo(description0.bottom, margin = 10.dp)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            SendAnimation()
        }
    }
}