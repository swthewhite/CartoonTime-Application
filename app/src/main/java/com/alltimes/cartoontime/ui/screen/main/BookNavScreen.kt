package com.alltimes.cartoontime.ui.screen.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.composable.Map
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BookNavScreen(viewModel: MainViewModel) {

    val clickedCartoon by viewModel.clickedCartoon.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (backButton, title, directionBox, distance, locationText, map) = createRefs()

        // 상단 바 메뉴
        Image(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Back Icon",
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .constrainAs(backButton) {
                    top.linkTo(parent.top, margin = 10.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.goScreen(ScreenType.BOOKDETAIL)
                    }
                )
        )


        Text(
            text = clickedCartoon.title,
            fontSize = 24.sp,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top, margin = 20.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.goScreen(ScreenType.BOOKRECOMMEND)
                    }
                )
                .constrainAs(directionBox) {
                    top.linkTo(title.bottom, margin = 10.dp)
                }
        ) {
            // 책이미지 (중앙에 위치)
            Image(
                painter = painterResource(id = R.drawable.image_book),
                contentDescription = "Book Image",
                modifier = Modifier
                    .size(150.dp) // 크기를 줄이기 위해 width와 height 대신 size 사용
                    .clip(CircleShape)
                    .align(Alignment.Center), // 중앙에 배치
                contentScale = ContentScale.Crop
            )

            // 방향 표시 이미지
            // 책 이미지의 바깥에 위치하도록 오프셋을 조절해야함
            // 방향 표시 이미지가 원형 경로를 따라 회전하도록 애니메이션 적용
            // Density를 가져와서 Dp를 px로 변환
            val density = LocalDensity.current

            // 반경을 Dp에서 Px로 변환
            val radiusPx = with(density) { 100.dp.toPx() }

            // 일단은 애니메이션으로 구현
            // 추후에 viewmodel에서 방향값을 받아서 계산하는 방식으로 변경
            // 애니메이션 정의
            val infiniteTransition = rememberInfiniteTransition()
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            // X와 Y 좌표 계산 (각도를 기준으로 위치 이동)
            val offsetX = radiusPx * cos(Math.toRadians(angle.toDouble())).toFloat()
            val offsetY = radiusPx * sin(Math.toRadians(angle.toDouble())).toFloat()

            // 각도를 계산해서 방향 이미지가 해당 방향을 가리키도록 회전
            val pointingAngle =
                Math.toDegrees(atan2(offsetY.toDouble(), offsetX.toDouble())).toFloat() + 135f


            // 방향 표시 이미지 (책 이미지 바깥을 회전)
            Image(
                painter = painterResource(id = R.drawable.image_direction),
                contentDescription = "Direction Image",
                modifier = Modifier
                    .size(50.dp) // 방향 이미지 크기
                    .graphicsLayer(
                        translationX = offsetX, // X 좌표 이동
                        translationY = offsetY, // Y 좌표 이동
                        rotationZ = pointingAngle // 방향 이미지 회전
                    )
                    .align(Alignment.Center), // 책 이미지 기준으로 위치
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = "거리",
            fontSize = 40.sp,
            modifier = Modifier.constrainAs(distance) {
                top.linkTo(directionBox.bottom, margin = 10.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Text(
            text = "도서 위치  " + clickedCartoon.sector + "  구역",
            fontSize = 20.sp,
            modifier = Modifier.constrainAs(locationText) {
                top.linkTo(distance.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(10.dp)
                .background(Color.Transparent)
                .constrainAs(map) {
                    top.linkTo(locationText.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            Map(viewModel, Pair(0.dp, 0.dp))
        }
    }

}