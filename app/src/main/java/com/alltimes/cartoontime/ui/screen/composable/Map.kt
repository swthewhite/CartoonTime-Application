package com.alltimes.cartoontime.ui.screen.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.Cartoon
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
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
                    .background(Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                if (clickedCartoon.sector == "A") {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }

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
                    .background(Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                if (clickedCartoon.sector == "D") {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }
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
                    .background(Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                if (clickedCartoon.sector == "G") {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }
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
                    .background(Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                if (clickedCartoon.sector == "B") {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }
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
                    .background(Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                if (clickedCartoon.sector == "E") {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }
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
                    .background(Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                if (clickedCartoon.sector == "H") {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }
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
                    .background(Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                if (clickedCartoon.sector == "C") {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }
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
                    .background(Color(0xFFF9B912))
                    .border(width = 1.dp, Color.Black)
            ) {
                if (clickedCartoon.sector == "F") {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }
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