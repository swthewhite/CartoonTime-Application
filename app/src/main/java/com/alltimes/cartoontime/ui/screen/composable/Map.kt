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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel

@Composable
fun Map(viewModel: MainViewModel) {

    val clickedCartoon by viewModel.clickedCartoon.collectAsState()

    Column(
        modifier = Modifier
            .background(Color(0xFFF4F2EE))
            .padding(5.dp)
            .fillMaxSize()
    ) {
        // A D G Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 25.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(30.dp)
                    .height(60.dp)
                    .background(if (clickedCartoon.sector == "A") Color.Red else Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {

                Text(
                    text = "A",
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.width(50.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(30.dp)
                    .height(60.dp)
                    .background(if (clickedCartoon.sector == "D") Color.Red else Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                Text(
                    text = "D",
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.width(50.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(30.dp)
                    .height(60.dp)
                    .background(if (clickedCartoon.sector == "G") Color.Red else Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                Text(
                    text = "G",
                    fontSize = 24.sp,
                )
            }
        }

        // B E H Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp, start = 25.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(30.dp)
                    .height(60.dp)
                    .background(if (clickedCartoon.sector == "B") Color.Red else Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                Text(
                    text = "B",
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.width(50.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(30.dp)
                    .height(60.dp)
                    .background(if (clickedCartoon.sector == "E") Color.Red else Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                Text(
                    text = "E",
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.width(50.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(30.dp)
                    .height(60.dp)
                    .background(if (clickedCartoon.sector == "H") Color.Red else Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                Text(
                    text = "H",
                    fontSize = 24.sp,
                )
            }
        }

        // C F Row 현
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp, start = 25.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(30.dp)
                    .height(60.dp)
                    .background(if (clickedCartoon.sector == "C") Color.Red else Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                Text(
                    text = "C",
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.width(50.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(30.dp)
                    .height(60.dp)
                    .background(if (clickedCartoon.sector == "F") Color.Red else Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                Text(
                    text = "F",
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.width(150.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(70.dp)
                    .height(60.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .background(Color(0xFF606060))
                ) {

                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "현재 위치",
                    fontSize = 15.sp,
                )
            }

        }

    }

}