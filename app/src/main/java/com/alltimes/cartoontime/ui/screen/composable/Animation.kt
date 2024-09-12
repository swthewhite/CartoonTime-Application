package com.alltimes.cartoontime.ui.screen.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alltimes.cartoontime.R
import kotlinx.coroutines.delay

@Composable
fun ApproachAnimate() {
    // 애니메이션을 위한 상태 값 (0.dp에서 150.dp 사이의 값을 애니메이션)
    // spacerWidth의 초기값을 150.dp로 설정
    var spacerWidth by remember { mutableStateOf(150.dp) }

    // 애니메이션 상태
    val animatedSpacerWidth by animateDpAsState(
        targetValue = spacerWidth,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing) // 1초 동안 애니메이션
    )

    // LaunchedEffect를 사용해 애니메이션 반복
    LaunchedEffect(Unit) {
        while (true) {
            spacerWidth = 0.dp // Spacer를 왼쪽으로 이동
            delay(1000) // 1초 대기
            delay(2000)
            spacerWidth = 150.dp // Spacer를 오른쪽으로 이동
            delay(1000) // 1초 대기
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F2EE)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 왼쪽 이미지
            Image(
                painter = painterResource(id = R.drawable.ic_kiosk),
                contentDescription = null,
                modifier = Modifier
                    .width(150.dp)
                    .height(200.dp)
                    .padding(start = 15.dp)
            )

            // 애니메이션되는 Spacer
            Spacer(modifier = Modifier.width(animatedSpacerWidth))

            // 오른쪽 이미지
            Image(
                painter = painterResource(id = R.drawable.ic_phone_front),
                contentDescription = null,
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .padding(start = 5.dp)
            )
        }
    }
}

@Composable
fun PhoneReverseAnimate() {
    // density 값을 로컬에서 가져옴
    val density = LocalDensity.current.density

    // 회전 각도 상태 (0f에서 180f로 회전)
    var rotationAngle by remember { mutableStateOf(0f) }

    // 애니메이션 상태
    val animatedRotationAngle by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    // 회전 후 정면 이미지인지 뒷면 이미지인지 결정
    val isFrontVisible = animatedRotationAngle < 90f

    // LaunchedEffect를 사용해 애니메이션 반복
    LaunchedEffect(Unit) {
        while (true) {
            rotationAngle = 180f // 휴대폰을 뒤집음
            delay(2000) // 뒤집은 후 2초 대기
            rotationAngle = 0f // 원래대로 복귀
            delay(2000) // 복귀 후 2초 대기
        }
    }


    // 휴대폰 애니메이션 영역
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                rotationY = animatedRotationAngle,
                cameraDistance = 12f * density // 회전 애니메이션의 심도 설정
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isFrontVisible) {
            // 정면 이미지
            Image(
                painter = painterResource(id = R.drawable.ic_phone_front),
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
        } else {
            // 뒷면 이미지
            Image(
                painter = painterResource(id = R.drawable.ic_phone_back),
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
        }
    }

}