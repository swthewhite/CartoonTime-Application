package com.alltimes.cartoontime.ui.screen.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alltimes.cartoontime.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

@Composable
fun SendDescription() {
    // Define the infinite transition for Spacer height
    val infiniteTransition = rememberInfiniteTransition()

    // Animate the spacer height from 200.dp to 0.dp and back
    val spacerHeight by infiniteTransition.animateFloat(
        initialValue = 200f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animate the spacer width from 0.dp to 150.dp and back
    val spacerWidth by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // State to control the visibility of the Spacer's delayed effect
    var isDelayVisible by remember { mutableStateOf(false) }

    LaunchedEffect(spacerHeight) {
        // Check if spacer height is 0
        if (spacerHeight == 0f) {
            while(true)
            {
            isDelayVisible = true
            delay(3000) // 1-second delay
            isDelayVisible = false

            }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 왼쪽 정렬
                Spacer(modifier = Modifier.width(spacerWidth.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_phone_front),
                    contentDescription = "Left Image",
                    modifier = Modifier.size(100.dp) // Keep the image size fixed
                )
            }
            Spacer(modifier = Modifier.height(spacerHeight.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 오른쪽 정렬
                Image(
                    painter = painterResource(id = R.drawable.ic_phone_back),
                    contentDescription = "Right Image",
                    modifier = Modifier.size(100.dp) // Keep the image size fixed
                )
                Spacer(modifier = Modifier.width(spacerWidth.dp))
            }
        }
    }
}

@Composable
fun SendAnimation() {
    val images = listOf(
        R.drawable.blue_color_book,
        R.drawable.green_color_book,  // 2번 이미지
        R.drawable.orange_color_book,
        R.drawable.red_color_book,    // 4번 이미지
        R.drawable.sky_color_book,
        R.drawable.yellow_book
    )

    // 각 이미지의 초기 Y 위치 (서로 겹치도록 설정)
    val initialOffsets = listOf(-150.dp, -88.dp, -50.dp, 12.dp, 50.dp, 100.dp)

    // 각 이미지의 Y 위치와 가시성을 관리하는 리스트
    val offsets = remember { List(images.size) { index -> Animatable(initialOffsets[index].value) } }
    val visibilities = remember { List(images.size) { Animatable(1f) } }

    LaunchedEffect(Unit) {
        while (true) {
            // 순차적으로 애니메이션 처리
            for (i in images.indices) {
                // y 위치를 -30.dp 이동
                offsets[i].animateTo(initialOffsets[i].value - 30f, animationSpec = tween(500))
                // alpha 값을 0으로 변경해서 invisible
                visibilities[i].animateTo(0f, animationSpec = tween(500))
                // 다음 이미지를 위해 0.25초 지연
                delay(250)
            }

            // 모든 애니메이션이 완료된 후 0.5초 대기
            delay(250)

            // 모든 이미지가 동시에 원래 상태로 되돌아오기 (y = 초기 위치, visible)
            offsets.forEachIndexed { index, animatable ->
                animatable.animateTo(initialOffsets[index].value, animationSpec = tween(0))
            }
            visibilities.forEach { visibility ->
                visibility.animateTo(1f, animationSpec = tween(0))
            }

            // 원래 상태로 되돌아온 후 0.5초 대기 후 다시 시작
            delay(250)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // 이미지 2번과 4번을 제외한 나머지 이미지를 먼저 렌더링
        images.forEachIndexed { index, image ->
            if (index != 1 && index != 3) {  // 2번(1)과 4번(3) 제외
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Image $index",
                    modifier = Modifier
                        .size(150.dp)
                        .offset(y = offsets[index].value.dp) // Float 값을 Dp로 변환
                        .graphicsLayer(alpha = visibilities[index].value)
                )
            }
        }

        // 이미지 2번 (맨 앞에 배치)
        Image(
            painter = painterResource(id = R.drawable.green_color_book),
            contentDescription = "Image 2",
            modifier = Modifier
                .size(150.dp)
                .offset(y = offsets[1].value.dp)
                .graphicsLayer(alpha = visibilities[1].value)
        )

        // 이미지 4번 (맨 앞에 배치)
        Image(
            painter = painterResource(id = R.drawable.red_color_book),
            contentDescription = "Image 4",
            modifier = Modifier
                .size(150.dp)
                .offset(y = offsets[3].value.dp)
                .graphicsLayer(alpha = visibilities[3].value)
        )
    }
}