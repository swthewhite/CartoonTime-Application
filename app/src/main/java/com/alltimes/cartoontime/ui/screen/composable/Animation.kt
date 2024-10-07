package com.alltimes.cartoontime.ui.screen.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.R
import kotlinx.coroutines.delay

@Composable
fun ApproachAnimate() {
    // 애니메이션을 위한 상태 값 (0.dp에서 150.dp 사이의 값을 애니메이션)
    // spacerWidth의 초기값을 150.dp로 설정
    var spacerWidth by remember { mutableStateOf(130.dp) }

    // 애니메이션 상태
    val animatedSpacerWidth by animateDpAsState(
        targetValue = spacerWidth,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing) // 1초 동안 애니메이션
    )

    // LaunchedEffect를 사용해 애니메이션 반복
    LaunchedEffect(Unit) {
        while (true) {
            spacerWidth = 0.dp // Spacer를 왼쪽으로 이동
            delay(3000) // 3초 대기
            spacerWidth = 130.dp // Spacer를 오른쪽으로 이동
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
                    .padding(start = 15.dp),
                contentScale = ContentScale.Crop
            )

            // 애니메이션되는 Spacer
            Spacer(modifier = Modifier.width(animatedSpacerWidth))

            // 오른쪽 이미지
            Image(
                painter = painterResource(id = R.drawable.ic_phone_front),
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .padding(start = 5.dp)
            )
        }
    }
}

@Composable
fun PhoneReverseAnimate() {
    // density 값을 로컬에서 가져옴
    val density = LocalDensity.current.density

    // 회전 각도 상태 (0f에서 -180f로 회전)
    var rotationAngle by remember { mutableStateOf(0f) }

    // 애니메이션 상태
    val animatedRotationAngle by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    // 회전 후 정면 이미지인지 뒷면 이미지인지 결정
    val isFrontVisible = animatedRotationAngle > -90f // 각도가 -90 이하일 때 뒷면 표시

    // LaunchedEffect를 사용해 애니메이션 반복
    LaunchedEffect(Unit) {
        while (true) {
            rotationAngle = -180f // 휴대폰을 뒤집음 (왼쪽으로 회전)
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
                    .width(150.dp)
                    .height(150.dp)
            )
        } else {
            // 뒷면 이미지
            Image(
                painter = painterResource(id = R.drawable.ic_phone_back),
                contentDescription = null,
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
            )
        }
    }
}

@Composable
fun Description() {
    // Define the infinite transition for Spacer height
    val infiniteTransition = rememberInfiniteTransition()

    // Animate the spacer height from 200.dp to 0.dp and back
    val spacerHeight by infiniteTransition.animateFloat(
        initialValue = 150f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animate the spacer width from 0.dp to 150.dp and back
    val spacerWidth by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
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
            while (true) {
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
                    modifier = Modifier
                        .size(150.dp) // Keep the image size fixed
                        .graphicsLayer { rotationZ = 180f } // Rotate the image
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
                    modifier = Modifier.size(150.dp) // Keep the image size fixed
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
    val offsets =
        remember { List(images.size) { index -> Animatable(initialOffsets[index].value) } }
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

            // 모든 애니메이션이 완료된 후 0.25초 대기
            delay(250)

            // 모든 이미지가 동시에 원래 상태로 되돌아오기 (y = 초기 위치, visible)
            offsets.forEachIndexed { index, animatable ->
                animatable.animateTo(initialOffsets[index].value, animationSpec = tween(0))
            }
            visibilities.forEach { visibility ->
                visibility.animateTo(1f, animationSpec = tween(0))
            }

            // 원래 상태로 되돌아온 후 0.25초 대기 후 다시 시작
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

@Composable
fun ReceiveAnimation() {
    val images = listOf(
        R.drawable.yellow_book,         // 1번 이미지
        R.drawable.sky_color_book,      // 2번 이미지
        R.drawable.red_color_book,      // 3번 이미지
        R.drawable.orange_color_book,   // 4번 이미지
        R.drawable.green_color_book,    // 5번 이미지
        R.drawable.blue_color_book      // 6번 이미지
    )

    // 각 이미지의 초기 Y 위치 (서로 겹치도록 설정)
    // 3번과 5번
    val initialOffsets = listOf(-180.dp, -130.dp, -68.dp, -30.dp, 32.dp, 70.dp)

    // 각 이미지의 Y 위치와 가시성을 관리하는 리스트
    val offsets =
        remember { List(images.size) { index -> Animatable(initialOffsets[index].value) } }
    val visibilities =
        remember { List(images.size) { Animatable(0f) } }  // 처음엔 전부 invisible (alpha = 0)

    LaunchedEffect(Unit) {
        while (true) {
            // 순차적으로 애니메이션 처리 (6번 이미지부터 1번 이미지까지)
            for (i in images.indices.reversed()) {
                // alpha 값을 1로 변경해서 visible
                visibilities[i].animateTo(1f, animationSpec = tween(500))
                // y 위치를 +30.dp 이동 (아래로 이동)
                offsets[i].animateTo(initialOffsets[i].value + 30f, animationSpec = tween(500))
                // 다음 이미지를 위해 0.25초 지연
                delay(250)
            }

            // 모든 애니메이션이 완료된 후 0.25초 대기
            delay(250)

            // 모든 이미지가 동시에 사라지기 (alpha = 0, 다시 invisible)
            visibilities.forEach { visibility ->
                visibility.animateTo(0f, animationSpec = tween(0))
            }

            // 모든 이미지가 원래 상태로 되돌아오기 (y = 초기 위치)
            offsets.forEachIndexed { index, animatable ->
                animatable.animateTo(initialOffsets[index].value, animationSpec = tween(0))
            }

            // 원래 상태로 되돌아온 후 0.25초 대기 후 다시 시작
            delay(250)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // 3번과 5번 이미지를 제외한 나머지 이미지 렌더링
        images.forEachIndexed { index, image ->
            if (index != 4 && index != 2) {  // 5번(1)과 3번(2) 제외
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Image $index",
                    modifier = Modifier
                        .size(150.dp)
                        .offset(y = offsets[index].value.dp)  // Float 값을 Dp로 변환
                        .graphicsLayer(alpha = visibilities[index].value)  // visibility 적용
                )
            }
        }

        // 3번 이미지 (가장 앞에 배치)
        Image(
            painter = painterResource(id = R.drawable.red_color_book),
            contentDescription = "Image 3",
            modifier = Modifier
                .size(150.dp)
                .offset(y = offsets[2].value.dp)
                .graphicsLayer(alpha = visibilities[2].value)
        )

        // 5번 이미지 (가장 앞에 배치)
        Image(
            painter = painterResource(id = R.drawable.green_color_book),
            contentDescription = "Image 5",
            modifier = Modifier
                .size(150.dp)
                .offset(y = offsets[4].value.dp)
                .graphicsLayer(alpha = visibilities[4].value)
        )
    }
}

@Composable
fun deviceFindingAnimation() {
    // 이미지 리소스 ID를 배열로 정의
    val imageIds = listOf(
        R.drawable.image_loading0, // 0번 이미지
        R.drawable.image_loading1, // 1번 이미지
        R.drawable.image_loading2  // 2번 이미지
    )

    // 각 이미지의 visible 상태를 관리하는 상태값
    var visibleImages by remember { mutableStateOf(listOf(false, false, false)) }

    LaunchedEffect(Unit) {
        while (true) {
            // 순차적으로 이미지가 visible로 전환
            for (i in imageIds.indices) {
                visibleImages = visibleImages.mapIndexed { index, _ -> index <= i }
                delay(1000)
            }
            // 모든 이미지 invisible 상태로 전환
            visibleImages = listOf(false, false, false)
            delay(1000)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 애니메이션 이미지들 (고정 이미지와 겹치도록 같은 Box에 배치)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 0.dp), // 고정 이미지와 약간 겹치게 오프셋을 줄여서 조정
            contentAlignment = Alignment.Center
        ) {
            imageIds.reversed().forEachIndexed { index, imageId ->
                val alpha by animateFloatAsState(if (visibleImages[2 - index]) 1f else 0f)

                Image(
                    painter = painterResource(imageId),
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer(alpha = alpha)
                        .width(300.dp)  // 이미지의 가로 크기
                        .height(300.dp) // 이미지의 세로 크기
                        .offset(y = -50.dp)
                )
            }
        }

        // 항상 보여지는 추가 이미지 (애니메이션 이미지들과 겹치도록 위치 조정)
        Image(
            painter = painterResource(id = R.drawable.ic_phone_front), // 고정 이미지 리소스
            contentDescription = "Phone Image",
            modifier = Modifier
                .width(120.dp)  // 이미지의 가로 크기
                .height(120.dp) // 이미지의 세로 크기
                .offset(x = 5.dp, y = 50.dp) // 약간 위로 올려 애니메이션 이미지와 겹치도록 설정
        )

        // 고정된 텍스트 (추가된 이미지 아래쪽에 위치)
        Text(
            text = "Send Witch를 켠 다른 장치를 찾고 있어요.",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier
                .offset(y = 150.dp) // 추가된 이미지 아래에 텍스트 위치
            ,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoadingAnimation() {
// 0번부터 15번까지의 이미지 리소스 ID 리스트
    val imageIds = (0..15).map { index ->
        when (index) {
            0 -> R.drawable.ic_loading0
            1 -> R.drawable.ic_loading1
            2 -> R.drawable.ic_loading2
            3 -> R.drawable.ic_loading3
            4 -> R.drawable.ic_loading4
            5 -> R.drawable.ic_loading5
            6 -> R.drawable.ic_loading6
            7 -> R.drawable.ic_loading7
            8 -> R.drawable.ic_loading8
            9 -> R.drawable.ic_loading9
            10 -> R.drawable.ic_loading10
            11 -> R.drawable.ic_loading11
            12 -> R.drawable.ic_loading12
            13 -> R.drawable.ic_loading13
            14 -> R.drawable.ic_loading14
            15 -> R.drawable.ic_loading15
            else -> R.drawable.ic_loading0 // 기본값
        }
    }

    // 각 이미지의 visible 상태를 관리하는 상태값
    var visibleImages by remember { mutableStateOf(List(16) { false }) }

    LaunchedEffect(Unit) {
        while (true) {
            // 순차적으로 이미지가 visible로 전환
            for (i in imageIds.indices) {
                visibleImages = visibleImages.mapIndexed { index, _ -> index <= i }
                delay(100) // 0.5초 대기
            }
            // 모든 이미지 invisible 상태로 전환
            visibleImages = List(16) { false }
            delay(500) // 0.5초 대기 후 다시 시작
        }
    }

    // 이미지를 화면의 중앙에 배치
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        imageIds.forEachIndexed { index, imageId ->
            val alpha by animateFloatAsState(if (visibleImages[index]) 1f else 0f)

            Image(
                painter = painterResource(id = imageId),
                contentDescription = "Loading Animation Image $index",
                modifier = Modifier
                    .graphicsLayer(alpha = alpha)
                    .size(200.dp) // 원하는 크기로 설정
            )
        }
    }
}