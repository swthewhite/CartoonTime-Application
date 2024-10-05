package com.alltimes.cartoontime.ui.screen.main

import android.util.Log
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.composable.GlideImage
import com.alltimes.cartoontime.ui.screen.composable.Map
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BookNavScreen(viewModel: MainViewModel) {

    val clickedCartoon by viewModel.clickedCartoon.collectAsState()

    // 실시간 좌표 및 방향 정보 수집
    val currentLocation by viewModel.currentLocation.collectAsState()
    val targetLocation by viewModel.targetLocation.collectAsState()

    // 거리 계산 및 포맷팅 (소수점 두 자리까지만 표시)
    val distance = viewModel.calculateDistance(currentLocation, targetLocation)
    val formattedDistance = String.format("%.2f", distance)

    // 목표 방향 계산
    val targetDirection = viewModel.calculateTargetDirection(currentLocation, targetLocation)

    // 방향 계산 (자이로 센서와 목표 좌표를 기반으로 계산)
    val direction = viewModel.direction.collectAsState()

    // 화면 크기 가져오기
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val mapHeightDp = 250.dp

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (backButton, title, directionBox, bookImage, distanceBox, locationText, map) = createRefs()

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


        clickedCartoon?.let {
            Text(
                text = it.titleKo,
                fontSize = 24.sp,
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(parent.top, margin = 20.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                }
            )
        }

        // 책 이미지와 방향 이미지 배치
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.Transparent)
                .constrainAs(directionBox) {
                    top.linkTo(title.bottom, margin = 10.dp)
                }
        ) {
            // 중앙의 책 이미지
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .align(Alignment.Center)
            ) {
                GlideImage(
                    url = clickedCartoon?.imageUrl ?: "",
                    width = 150.dp,
                    height = 150.dp
                )
            }

            // 방향 이미지 애니메이션
            val density = LocalDensity.current
            val radiusPx = with(density) { 100.dp.toPx() }

            // 방향 표시 이미지가 현재 방향을 가리키도록 설정
            val pointingAngle = -direction.value + targetDirection

            // 새로운 오프셋 계산
            val offsetX = radiusPx * cos(Math.toRadians(pointingAngle.toDouble())).toFloat()
            val offsetY = radiusPx * sin(Math.toRadians(pointingAngle.toDouble())).toFloat()

            // 방향 표시 이미지
            Image(
                painter = painterResource(id = R.drawable.image_direction),
                contentDescription = "Direction Image",
                modifier = Modifier
                    .size(50.dp)
                    .graphicsLayer(
                        translationX = offsetX,
                        translationY = offsetY,
                        rotationZ = pointingAngle + 135f // 회전 각도
                    )
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )
        }

        // 거리 및 방향 텍스트
        Text(
            text = "거리: ${formattedDistance}m",
            fontSize = 40.sp,
            modifier = Modifier.constrainAs(distanceBox) {
                top.linkTo(directionBox.bottom, margin = 10.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Text(
            text = "도서 위치  " + clickedCartoon?.location + "  구역",
            fontSize = 20.sp,
            modifier = Modifier.constrainAs(locationText) {
                top.linkTo(distanceBox.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeightDp)
                .padding(10.dp)
                .background(Color.Transparent)
                .constrainAs(map) {
                    top.linkTo(locationText.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            // 지도 그리드에 맞춘 좌표
            Map(viewModel, currentLocation)
        }
    }

}