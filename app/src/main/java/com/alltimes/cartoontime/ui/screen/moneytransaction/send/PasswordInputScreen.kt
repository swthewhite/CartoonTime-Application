package com.alltimes.cartoontime.ui.screen.moneytransaction.send

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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

import com.alltimes.cartoontime.ui.screen.composable.ApproachAnimate
import com.alltimes.cartoontime.ui.screen.composable.PhoneReverseAnimate
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.viewmodel.SendViewModel


@Composable
fun PasswordInputScreen(viewModel: SendViewModel) {

    val imgSize = 40.dp
    val imgSpace = 10.dp
    val btnSpace = 80.dp


    val point by viewModel.point.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val password by viewModel.password.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (backButton, title, description0, description1, passwordRow, description2, numPad, ) = createRefs()

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
                        viewModel.goScreen(ScreenType.POINTINPUT)
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

        Text(
            text = "간편 비밀번호",
            fontSize = 36.sp,
            color = Color.Black,
            modifier = Modifier
                .constrainAs(description0) {
                    top.linkTo(title.bottom, margin = 50.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "비밀번호를 입력해주세요.",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier
                .constrainAs(description1) {
                    top.linkTo(description0.bottom, margin = 50.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )


        // 비밀번호 6자리 표시
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp, end = 50.dp)
                .constrainAs(passwordRow) {
                    top.linkTo(description1.bottom, margin = 50.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(6) { i ->
                Image(
                    painter = if (password.length > i)
                        painterResource(id = R.drawable.ic_filled_circle)
                    else painterResource(id = R.drawable.ic_empty_circle),
                    contentDescription = "password${i + 1}",
                    modifier = Modifier
                        .size(imgSize)
                        .padding(horizontal = imgSpace)
                )
            }
        }

        // 비밀번호를 잊으셨나요 ?
        Box(
            modifier = Modifier
                .constrainAs(description2) {
                    bottom.linkTo(numPad.top, 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ){
            Text(
                text = "비밀번호를 잊어버리셨나요?",
                fontSize = 16.sp,
                color = Color.Gray,
            )
        }


        // 숫자패드
        // 0 ~ 9, 삭제 버튼
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(numPad) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            // 숫자 버튼들
            (1..9 step 3).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    (row until row + 3).forEach { num ->
                        Button(
                            onClick = { viewModel.onPasswordClickedButton(num) },
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                            modifier = Modifier
                                .weight(1f)
                                .height(btnSpace)
                        ) {
                            Text(
                                text = num.toString(),
                                color = Color(0xFF000000),
                                fontSize = 40.sp
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {  },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)
                ) {
                    Text(" ", color = Color(0xFF000000), fontSize = 40.sp)
                }
                Button(
                    onClick = { viewModel.onPasswordClickedButton(0) },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)
                ) {
                    Text("0", color = Color(0xFF000000), fontSize = 40.sp)
                }
                Button(
                    onClick = { viewModel.onPasswordClickedButton(-1) },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier
                        .weight(1f)
                        .height(btnSpace)
                ) {
                    Text("<-", color = Color(0xFF000000), fontSize = 40.sp)
                }
            }
        }
    }

}