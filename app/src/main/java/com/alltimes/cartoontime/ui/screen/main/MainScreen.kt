package com.alltimes.cartoontime.ui.screen.main


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
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import com.alltimes.cartoontime.ui.screen.composable.ApproachAnimate
import com.alltimes.cartoontime.ui.screen.composable.PhoneReverseAnimate
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.composable.LoadingAnimation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

@Composable
fun MainScreen(viewModel: MainViewModel) {
    var currentAnimation by remember { mutableStateOf(1) }
    var showExitDialog by remember { mutableStateOf(false) }

    val name = viewModel.name
    val balance by viewModel.balance.collectAsState()
    val state by viewModel.state.collectAsState()
    val enteredTime by viewModel.enteredTime.collectAsState()
    
    // 입실 중
    val uiState by viewModel.uiState.collectAsState()
    val active = remember { mutableStateOf(false) }

    // 입실 완료
    var usedTime by remember { mutableStateOf("") }

// 실시간으로 현재 시간과 enteredTime 차이를 계산하는 LaunchedEffect
    LaunchedEffect(enteredTime) {
        while (true) {
            val formatterWithSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formatterWithoutSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

            val entered = try {
                LocalDateTime.parse(enteredTime, formatterWithSeconds)
            } catch (e: DateTimeParseException) {
                // 초가 없는 경우
                LocalDateTime.parse(enteredTime, formatterWithoutSeconds).withSecond(0)
            }

            val now = LocalDateTime.now()

            // 시간 차이 계산
            val hours = ChronoUnit.HOURS.between(entered, now)
            val minutes = ChronoUnit.MINUTES.between(entered, now) % 60

            usedTime = if (hours > 0) {
                String.format("%d시간 %d분", hours, minutes)
            } else {
                String.format("%d분", minutes)
            }

            delay(1000) // 1초마다 업데이트
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (logo, line1, title, walletContainer, animationBox, stateText, stateTextDescription, bookButton) = createRefs()

        // 상단 로고 및 문구
        Image(
            painter = painterResource(id = R.drawable.logo_cartoontime),
            contentDescription = "Main Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(300.dp)
                .height(100.dp)
                .constrainAs(logo) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Row(
            modifier = Modifier
                .constrainAs(line1) {
                top.linkTo(logo.bottom, margin = 10.dp)
                    start.linkTo(parent.start, margin = 30.dp)
                    end.linkTo(parent.end, margin = 30.dp) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_line),
                contentDescription = "Line Icon",
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp)
                    .padding(end = 10.dp)
            )
            Text(text = "만화와 소중했던 그 시간들")
            Image(
                painter = painterResource(id = R.drawable.ic_line),
                contentDescription = "Line Icon",
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp)
                    .padding(start = 10.dp)
            )
        }

        Text(
            text = "${name}님 환영합니다.",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier
                .constrainAs(title) {
                    top.linkTo(line1.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                }
        )

        // 지갑 컨테이너
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(Color(0xFFF9B912), RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .constrainAs(walletContainer) {
                    top.linkTo(title.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_wallet),
                        contentDescription = "Icon",
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "내 지갑",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
                ) {
                    Text(
                        text = "$balance Points",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.goActivity(ActivityType.SEND) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A911)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = "송금하기", color = Color.Black)
                    }
                    Button(
                        onClick = { viewModel.goActivity(ActivityType.RECEIVE)
                                  },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A911)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = "송금받기", color = Color.Black)
                    }
                    Button(
                        onClick = { viewModel.goActivity(ActivityType.CHARGE) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A911)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = "충전", color = Color.Black)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 5.dp)
                .constrainAs(animationBox) {
                    top.linkTo(walletContainer.bottom, margin = 5.dp)
                    start.linkTo(parent.start, margin = 5.dp)
                    end.linkTo(parent.end, margin = 5.dp)
                }
        ) {
            if (state == "입실 전") {
                AnimateSequence { animationId ->
                    currentAnimation = animationId
                }
            } else if (state == "입실 완료") {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFFFFF), RoundedCornerShape(16.dp))
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(5.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "입실 시간", fontSize = 20.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "이용 시간", fontSize = 20.sp, color = Color.Black)
                            }

                            Spacer(modifier = Modifier.width(40.dp))

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 5.dp)
                            ) {
                                Text(text = "$enteredTime", fontSize = 20.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "$usedTime", fontSize = 20.sp, color = Color.Black)
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        showExitDialog = true
                                    }
                                )
                        ) {
                            Text(
                                text = "퇴실 방법",
                                color = Color(0xFF413930),
                                fontSize = 20.sp,
                                style = TextStyle(textDecoration = TextDecoration.Underline)
                            )
                        }
                    }
                }
            }
            // 입실 중 , 퇴실 중
            else {
                LoadingAnimation()
            }
        }

        Text(
            text = "$state",
            fontSize = 20.sp,
            modifier = Modifier
                .constrainAs(stateText) {
                top.linkTo(animationBox.bottom, margin = 10.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        if (state == "입실 전") {
            Text(
                text = if (currentAnimation == 1) {
                    "핸드폰을 키오스크 가까이에 갖다 대주세요."
                } else {
                    "손 안의 핸드폰을 뒤집어서 입실을 진행해주세요."
                },
                fontSize = 15.sp,
                modifier = Modifier
                    .constrainAs(stateTextDescription) {
                        top.linkTo(stateText.bottom, margin = 10.dp)
                        bottom.linkTo(parent.bottom, margin = 5.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        } else if (state == "입실 중") {
            Text(
                text = "잠시만 기다려주세요.",
                fontSize = 15.sp,
                modifier = Modifier
                    .constrainAs(stateTextDescription) {
                        top.linkTo(stateText.bottom, margin = 10.dp)
                        bottom.linkTo(parent.bottom, margin = 5.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        } else if (state == "입실 완료") {
            Button(
                onClick = { viewModel.goScreen(ScreenType.BOOKRECOMMEND) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(350.dp)
                    .height(50.dp)
                    .padding(start = 8.dp)
                    .constrainAs(bookButton) {
                        top.linkTo(stateText.bottom, margin = 10.dp)
                        bottom.linkTo(parent.bottom, margin = 5.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Text(text = "추천만화 확인하기", color = Color.Black)
            }
        }
    }

    // 팝업 표시
    if (showExitDialog) {
        ExitDialog(onDismiss = { showExitDialog = false })
    }

    // 입퇴실
    if (!active.value && uiState.isLogin) {
        viewModel.onKioskLoadingCompleted()
        active.value = true
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

@Composable
fun ExitDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF4F2EE), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "퇴실 방법 안내",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "입실방법과 똑같이\n키오스크 가까이에서\n휴대폰을 뒤집으면 퇴실이 완료돼요.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A911)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "닫기", color = Color.Black)
                }
            }
        }
    }
}