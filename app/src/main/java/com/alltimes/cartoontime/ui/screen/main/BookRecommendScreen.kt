package com.alltimes.cartoontime.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.composable.Map
import com.alltimes.cartoontime.ui.screen.composable.cartoon
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel

@Composable
fun BookRecommendScreen(viewModel: MainViewModel) {
    val name = viewModel.name
    val cartoons by viewModel.cartoons.collectAsState()
    val clickedCartoon by viewModel.clickedCartoon.collectAsState()
    val category by viewModel.category.collectAsState()

    val userString = "$name" + "님의 취향을 바탕으로 추천한 만화입니다."
    val bestString = "주간 베스트 셀러 만화입니다."
    val todayString = "오늘의 추천 만화입니다."

    viewModel.bookRecommendInit()

    LaunchedEffect(category) {
        viewModel.fetchCartoons(category)
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (homeButton, title, descriptionBox, description, map, categoryRow, recommendationText, cartoonList, detailButton) = createRefs()

        // 제일 위 홈버튼과 이름

        Box(
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .constrainAs(homeButton) {
                    top.linkTo(parent.top, margin = 10.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { viewModel.goScreen(ScreenType.MAIN) }
                ),

            ) {
            Image(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Text(
            text = "추천만화",
            fontSize = 30.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(homeButton.top)   // 이미지의 상단에 맞추고
                bottom.linkTo(homeButton.bottom)  // 이미지의 하단에 맞춤 (세로 중앙 정렬)
                start.linkTo(parent.start, margin = 10.dp)  // 홈버튼 오른쪽에 위치
                end.linkTo(parent.end, margin = 10.dp)
                width = Dimension.wrapContent
            }
        )

        // 상단 설명
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(40.dp)
                .background(Color(0xFFF9B912), RoundedCornerShape(8.dp))
                .constrainAs(descriptionBox) {
                    top.linkTo(homeButton.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                }
        )

        Text(
            text = "만화 위치",
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(description) {
                top.linkTo(descriptionBox.top)
                bottom.linkTo(descriptionBox.bottom)
                start.linkTo(descriptionBox.end, margin = 10.dp)
            }
        )

        // 지도
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(5.dp)
                .background(Color.Transparent)
                .constrainAs(map) {
                    top.linkTo(description.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Map(viewModel)
        }

        // 카테고리
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .constrainAs(categoryRow) {
                    top.linkTo(map.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            listOf(
                "사용자 취향 만화" to "사용자 취향 만화",
                "베스트 셀러 만화" to "베스트 셀러 만화",
                "오늘의 추천 만화" to "오늘의 추천 만화"
            ).forEachIndexed { index, (label, value) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(50.dp)
                        .weight(1f)
                        .clickable {
                            viewModel.onClickedCategory(value)
                        }
                        .background(
                            color = if (category == value) Color(0xFFF9B912) else Color.Transparent,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .border(
                            1.dp,
                            if (category == value) Color(0xFFF9B912) else Color(0xFFA5A5A5),
                            RoundedCornerShape(0.dp)
                        )
                ) {
                    Text(
                        text = label,
                        color = if (category == value) Color(0xFF000000) else Color(0xFFA5A5A5),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Text(
            text = when (category) {
                "사용자 취향 만화" -> userString
                "베스트 셀러 만화" -> bestString
                else -> todayString
            },
            fontSize = 20.sp,
            modifier = Modifier.constrainAs(recommendationText) {
                top.linkTo(categoryRow.bottom, margin = 10.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        // 만화 추천 리스트 뷰
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .background(Color.Transparent)
                .constrainAs(cartoonList) {
                    top.linkTo(recommendationText.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            items(cartoons) { cartoon ->
                cartoon(cartoon, viewModel)
            }
        }

        if (clickedCartoon.title.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(250.dp)
                    .height(40.dp)
                    .background(Color(0xFFF9B912), RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.goScreen(ScreenType.BOOKDETAIL) }
                    )
                    .constrainAs(detailButton) {
                        bottom.linkTo(parent.bottom, margin = 10.dp)
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
    }
}