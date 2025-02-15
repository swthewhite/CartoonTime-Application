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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.composable.Pointpad
import com.alltimes.cartoontime.ui.viewmodel.SendViewModel

@Composable
fun SendPointInputScreen(viewModel: SendViewModel) {

    // viewmodel variable
    val point by viewModel.point.collectAsState()
    val balance by viewModel.balance.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (backButton, title, description0, description1, description2, points, nextButton, numPad) = createRefs()

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
                        viewModel.goActivity(ActivityType.MAIN)
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

        // 내 지갑에서
        Text(
            text = "내 지갑에서",
            fontSize = 26.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(description0) {
                top.linkTo(backButton.bottom, margin = 50.dp)
                start.linkTo(parent.start, margin = 15.dp)
                width = Dimension.wrapContent
            }
        )

        // 잔여 포인트
        Text(
            text = "잔여 포인트 ${balance}",
            fontSize = 26.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(description1) {
                top.linkTo(description0.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 15.dp)
                width = Dimension.wrapContent
            }
        )

        // 상대방 지갑으로
        Text(
            text = "상대방 지갑으로",
            fontSize = 26.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(description2) {
                top.linkTo(description1.bottom, margin = 35.dp)
                start.linkTo(parent.start, margin = 15.dp)
                width = Dimension.wrapContent
            }
        )

        // 얼마를 보낼까요 ?
        Text(
            text = if (point == "") "얼마를 보낼까요?" else "$point 포인트",
            fontSize = 40.sp,
            color = if (point == "") Color(0xFFA5A5A5) else Color.Black,
            modifier = Modifier.constrainAs(points) {
                top.linkTo(description2.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 15.dp)
                width = Dimension.wrapContent
            }
        )

        // 다음 버튼 visible
        if (point != "") {
            if (point.toInt() > 0) {
                Button(
                    onClick = {
                        viewModel.goScreen(ScreenType.SENDPASSWORDINPUT)
                        viewModel.initializePassword()
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                    modifier = Modifier
                        .width(350.dp)
                        .height(50.dp)

                        .constrainAs(nextButton) {
                            bottom.linkTo(numPad.top, margin = 10.dp)
                            start.linkTo(parent.start, margin = 15.dp)
                            end.linkTo(parent.end, margin = 15.dp)
                        }
                ) {
                    Text(
                        text = "다음",
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            }
        }

        // 숫자패드
        // 0 ~ 9, 삭제 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(numPad) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Pointpad(viewModel)
        }

    }
}