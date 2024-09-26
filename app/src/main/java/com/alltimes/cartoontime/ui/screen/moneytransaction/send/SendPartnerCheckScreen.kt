package com.alltimes.cartoontime.ui.screen.moneytransaction.send

import android.accessibilityservice.AccessibilityService.ScreenshotResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.alltimes.cartoontime.ui.screen.composable.deviceFindingAnimation
import com.alltimes.cartoontime.ui.viewmodel.SendViewModel
import kotlinx.coroutines.delay

@Composable
fun SendPartnerCheckScreen(viewModel: SendViewModel) {

    // viewmodel variable
    val uiState = viewModel.uiState.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (backButton, title, animationBox) = createRefs()

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

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(animationBox) {
                    top.linkTo(title.bottom, margin = 20.dp)
                    bottom.linkTo(parent.bottom, margin = 20.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            // 장치 연결 여부에 따른 UI 구현
            if (!uiState.value.isDeviceConnected) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Column 안에서 수평 중앙 정렬
                    modifier = Modifier.offset(y = (-50).dp) // 화면 중앙에서 살짝 위로 올림
                ) {
                    // 현재 연결된 디바이스 정보 텍스트
                    Text(
                        text = "NickName",
                        fontSize = 40.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(50.dp)) // 텍스트와 버튼 사이에 간격 추가

                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f), // Row를 화면 가로 크기의 80%로 설정
                        horizontalArrangement = Arrangement.SpaceBetween // 수락/거절 버튼을 양쪽 끝으로 정렬
                    ) {
                        // 수락 버튼
                        Button(
                            onClick = {
                                viewModel.startTransaction()
                            },
                            shape = RoundedCornerShape(15),
                            colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                            modifier = Modifier.weight(1f) // 버튼들을 동일한 크기로 설정
                        ) {
                            Text("Accept")
                        }

                        Spacer(modifier = Modifier.width(16.dp)) // 버튼 사이에 간격 추가

                        // 거절 버튼
                        Button(
                            onClick = {
                                viewModel.setUiState(!uiState.value.isDeviceConnected)
                            },
                            shape = RoundedCornerShape(15),
                            colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                            modifier = Modifier.weight(1f) // 버튼들을 동일한 크기로 설정
                        ) {
                            Text("Reject")
                        }
                    }
                }

            } else {
                // 연결된 장치가 없을 경우 애니메이션
                deviceFindingAnimation()
            }
        }
    }

}