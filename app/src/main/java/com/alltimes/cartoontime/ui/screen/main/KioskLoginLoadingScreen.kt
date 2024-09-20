package com.alltimes.cartoontime.ui.screen.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.collectAsState
import com.alltimes.cartoontime.data.model.ui.ScreenType

@Composable
fun KioskLoginLoadingScreen(viewModel: MainViewModel) {

    val uiState by viewModel.uiState.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (titleRef, animationRef) = createRefs()

        // 타이틀 텍스트
        Text(
            text = "Connecting to Kiosk...",
            fontSize = 30.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.constrainAs(titleRef) {
                top.linkTo(parent.top, margin = 40.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        // 애니메이션
        SunLoadingAnimation(
            modifier = Modifier
                .size(200.dp)
                .constrainAs(animationRef) {
                    top.linkTo(titleRef.bottom, margin = 40.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        if (uiState.isLogin) {
            // 장치가 연결되었을 때 UI 업데이트
            viewModel.onKioskLoadingCompleted()
        }
    }
}

@Composable
fun SunLoadingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()

    // 애니메이션으로 각 햇살의 색상이 변경됨
    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 2000, easing = LinearEasing)
        )
    )

    Canvas(modifier = modifier) {
        drawSunRays(animatedRotation)
    }
}

fun DrawScope.drawSunRays(rotation: Float) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.minDimension / 4

    // 햇살 12개 그리기
    for (i in 0 until 12) {
        rotate(rotation + i * 30f, pivot = Offset(centerX, centerY)) {
            drawRay(centerX, centerY, radius)
        }
    }

    // 가운데 원 그리기
    drawCircle(
        color = Color.Yellow,
        radius = radius,
        center = Offset(centerX, centerY)
    )
}

fun DrawScope.drawRay(centerX: Float, centerY: Float, radius: Float) {
    drawLine(
        color = Color(0xFFFFA500), // 주황색
        start = Offset(centerX, centerY),
        end = Offset(centerX, centerY - radius * 2),
        strokeWidth = 10f
    )
}