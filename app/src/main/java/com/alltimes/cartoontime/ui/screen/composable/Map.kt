package com.alltimes.cartoontime.ui.screen.composable

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel

@Composable
fun Map(viewModel: MainViewModel, offset: Pair<Dp, Dp>) {

    val clickedCartoon by viewModel.clickedCartoon.collectAsState()
    val offsetX = 300.dp
    val offsetY = 150.dp

    Box(
        modifier = Modifier
            .background(Color(0xFFF4F2EE))
            .padding(5.dp)
            .fillMaxSize()
    ) {
        // 현재 위치
        Box(
            modifier = Modifier
                .offset(
                    x = offsetX - offset.first,
                    y = offsetY - offset.second
                ) // 현재 위치 표시를 원하는 좌표에 배치
                .size(70.dp, 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0xFF606060)),
//                    contentAlignment = Alignment.Center
                )

                if (offset.first == 0.dp && offset.second == 0.dp) {
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(text = "현재 위치", fontSize = 15.sp)
                }
            }
        }


        // A D G Box
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 20.dp) // 원하는 위치로 이동
                .size(30.dp, 60.dp)
                .background(if (clickedCartoon?.location == "A") Color.Red else Color(0xFFF9B912))
                .border(width = 1.dp, Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "A", fontSize = 24.sp)
        }

        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = 20.dp)
                .size(30.dp, 60.dp)
                .background(if (clickedCartoon?.location == "D") Color.Red else Color(0xFFF9B912))
                .border(width = 1.dp, Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "D", fontSize = 24.sp)
        }

        Box(
            modifier = Modifier
                .offset(x = 180.dp, y = 20.dp)
                .size(30.dp, 60.dp)
                .background(if (clickedCartoon?.location == "G") Color.Red else Color(0xFFF9B912))
                .border(width = 1.dp, Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "G", fontSize = 24.sp)
        }

        // B E H Box
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 85.dp) // 원하는 위치로 이동
                .size(30.dp, 60.dp)
                .background(if (clickedCartoon?.location == "B") Color.Red else Color(0xFFF9B912))
                .border(width = 1.dp, Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "B", fontSize = 24.sp)
        }

        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = 85.dp)
                .size(30.dp, 60.dp)
                .background(if (clickedCartoon?.location == "E") Color.Red else Color(0xFFF9B912))
                .border(width = 1.dp, Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "E", fontSize = 24.sp)
        }

        Box(
            modifier = Modifier
                .offset(x = 180.dp, y = 85.dp)
                .size(30.dp, 60.dp)
                .background(if (clickedCartoon?.location == "H") Color.Red else Color(0xFFF9B912))
                .border(width = 1.dp, Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "H", fontSize = 24.sp)
        }

        // C F Box
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 150.dp) // 원하는 위치로 이동
                .size(30.dp, 60.dp)
                .background(if (clickedCartoon?.location == "C") Color.Red else Color(0xFFF9B912))
                .border(width = 1.dp, Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "C", fontSize = 24.sp)
        }

        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = 150.dp)
                .size(30.dp, 60.dp)
                .background(if (clickedCartoon?.location == "F") Color.Red else Color(0xFFF9B912))
                .border(width = 1.dp, Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "F", fontSize = 24.sp)
        }
    }

}

fun Offset.toDp(): Pair<Dp, Dp> {
    return Pair(this.x.dp, this.y.dp)
}