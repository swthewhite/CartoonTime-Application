package com.alltimes.cartoontime.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.model.uwb.Location
import com.alltimes.cartoontime.ui.screen.composable.GlideImage
import com.alltimes.cartoontime.ui.screen.composable.Loading
import com.alltimes.cartoontime.ui.screen.composable.Map
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel

@Composable
fun BookDetailScreen(viewModel: MainViewModel) {

    val networkStatus by viewModel.networkStatus.collectAsState()
    val clickedCartoon by viewModel.clickedCartoon.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (backButton, bookImage, title, author, genre, locationText, map, navButton) = createRefs()

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
                        viewModel.goScreen(ScreenType.BOOKRECOMMEND)
                    }
                )
        )


        Box(
            modifier = Modifier
                .width(200.dp)
                .height(300.dp)
                .constrainAs(bookImage) {
                    top.linkTo(parent.top, margin = 20.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                }
        ) {
            GlideImage(
                url = clickedCartoon?.imageUrl ?: "", // URL이 없을 경우 빈 문자열 처리
                width = 200.dp,
                height = 300.dp
            )
        }

        clickedCartoon?.let {
            Text(
                text = it.titleKo,
                fontSize = 24.sp,
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(bookImage.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                }
            )
        }

        clickedCartoon?.let {
            Text(
                text = it.authorKo,
                fontSize = 12.sp,
                modifier = Modifier.constrainAs(author) {
                    top.linkTo(title.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                }
            )
        }

        clickedCartoon?.genres?.get(0)?.let {
            Text(
                text = it.genreNameKo,
                fontSize = 12.sp,
                modifier = Modifier.constrainAs(genre) {
                    top.linkTo(author.bottom, margin = 10.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                }
            )
        }

        Text(
            text = "도서 위치  " + clickedCartoon?.location + "  구역",
            fontSize = 20.sp,
            modifier = Modifier.constrainAs(locationText) {
                top.linkTo(genre.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Box(
            modifier = Modifier
                .width(300.dp)
                .height(250.dp)
                .background(Color.Transparent)
                .constrainAs(map) {
                    top.linkTo(locationText.bottom, margin = 5.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Map(viewModel, Location(0f, 0f))
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(250.dp)
                .height(40.dp)
                .background(Color(0xFFF9B912), RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.goScreen(ScreenType.BOOKNAV)
                        viewModel.initializeMQTT()
                        viewModel.initializeSensors()
                    }
                )
                .constrainAs(navButton) {
                    top.linkTo(map.bottom, margin = 5.dp)
                    bottom.linkTo(parent.bottom, margin = 5.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = "해당 만화책 위치 경로 탐색",
                fontSize = 20.sp,
                color = Color(0xFF606060),
                textAlign = TextAlign.Center
            )
        }
    }

    // 인터넷 로딩 다이얼로그 표시
    networkStatus?.let { Loading("인터넷 연결 시도중 ... ", isLoading = it, onDismiss = { /* Dismiss Logic */ }) }

}