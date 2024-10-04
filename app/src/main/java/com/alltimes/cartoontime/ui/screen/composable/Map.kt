package com.alltimes.cartoontime.ui.screen.composable

import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.alltimes.cartoontime.data.model.uwb.Location
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel

@Composable
fun Map(viewModel: MainViewModel, currentLocation: Location) {

    val clickedCartoon by viewModel.clickedCartoon.collectAsState()

    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .background(Color(0xFFF4F2EE))
            .fillMaxSize()
            .onSizeChanged { size -> boxSize = size }
    ) {
        // 화면의 넓이와 높이
        val screenWidth = boxSize.width.toDp() // width를 Dp로 변환
        val screenHeight = 250.dp // y 좌표의 기준 높이

        // x와 y 좌표 매핑
        val xMapped = (currentLocation.x / 10f) * screenWidth // x는 0~10을 기준으로 화면 너비에 맞춰 매핑
        val yMapped = (currentLocation.y / 8f) * screenHeight // y는 0~8을 기준으로 250.dp에 맞춰 매핑


        // 현재 위치
        Box(
            modifier = Modifier
                .offset(x = xMapped, y = yMapped)
                .size(70.dp, 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .background(Color(0xFF606060))
            )

            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "현재 위치", fontSize = 15.sp)
        }

        // 좌표 박스 (A, D, G)
        LocationBox("A", Color.Red, clickedCartoon?.location == "A", 80.dp, 40.dp)
        LocationBox("D", Color.Red, clickedCartoon?.location == "D", 160.dp, 40.dp)
        LocationBox("G", Color.Red, clickedCartoon?.location == "G", 240.dp, 40.dp)

        LocationBox("B", Color.Red, clickedCartoon?.location == "B", 100.dp, 145.dp)
        LocationBox("E", Color.Red, clickedCartoon?.location == "E", 180.dp, 145.dp)
        LocationBox("H", Color.Red, clickedCartoon?.location == "H", 260.dp, 145.dp)

    }
}

// Dp 변환을 위한 확장 함수
fun Int.toDp(): Dp = (this / Resources.getSystem().displayMetrics.density).dp

@Composable
fun LocationBox(label: String, color: Color, isSelected: Boolean, x: Dp, y: Dp) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(30.dp, 60.dp)
            .background(if (isSelected) Color.Red else Color(0xFFF9B912))
            .border(width = 1.dp, Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 24.sp)
    }
}