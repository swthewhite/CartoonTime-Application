package com.alltimes.cartoontime.ui.screen.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import com.alltimes.cartoontime.ui.screen.composable.ApproachAnimate
import com.alltimes.cartoontime.ui.screen.composable.PhoneReverseAnimate
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.alltimes.cartoontime.ui.screen.composable.Map
import com.alltimes.cartoontime.ui.screen.composable.cartoon
import kotlinx.coroutines.delay

@Composable
fun BookRecommendScreen(viewModel: MainViewModel) {

    val name = viewModel.userName

    // 동적 구성 요소
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 제일 위 홈버튼과 이름
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(10.dp),
        ){
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(70.dp)
                    .background(Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable(
                        // 밑에 두개 다 써야 클릭시 효과 제거 가능
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            viewModel.onClickedHome()
                        }
                    )
            )  {
                Image(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home",
                    modifier = Modifier
                        .width(70.dp)
                        .height(70.dp)
                )
            }

            Spacer(modifier = Modifier.width(70.dp))

            Text(
                text = "추천만화",
                fontSize = 36.sp,
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 상단 설명
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp)
        ){
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(40.dp)
                    .background(Color(0xFFF9B912), RoundedCornerShape(8.dp))
            ){

            }

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = "만화 위치",
                fontSize = 18.sp,
                color = Color.Black
            )
        }

        // 지도
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(5.dp)
                .background(Color.Transparent)
        ){
            Map(viewModel)
        }

        // 카테고리
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f)
                    .clickable {
                        viewModel.onClickedCategory("사용자 취향 만화")
                    }
                    .background(
                        color = if (category == "사용자 취향 만화") Color(0xFFF9B912) else Color.Transparent,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .border(1.dp, if (category == "사용자 취향 만화") Color(0xFFF9B912) else Color(0xFFA5A5A5), RoundedCornerShape(0.dp))
            ) {
                Text(
                    text = "사용자 취향 만화",
                    color = if (category == "사용자 취향 만화") Color(0xFF000000) else Color(0xFFA5A5A5),
                    fontSize = 14.sp
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f)
                    .clickable {
                        viewModel.onClickedCategory("베스트 셀러 만화")
                    }
                    .background(
                        color = if (category == "베스트 셀러 만화") Color(0xFFF9B912) else Color.Transparent,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .border(1.dp, if (category == "베스트 셀러 만화") Color(0xFFF9B912) else Color(0xFFA5A5A5), RoundedCornerShape(0.dp))
            ) {
                Text(
                    text = "베스트 셀러 만화",
                    color = if (category == "베스트 셀러 만화") Color(0xFF000000) else Color(0xFFA5A5A5),
                    fontSize = 14.sp
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f)
                    .clickable {
                        viewModel.onClickedCategory("오늘의 추천 만화")
                    }
                    .background(
                        color = if (category == "오늘의 추천 만화") Color(0xFFF9B912) else Color.Transparent,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .border(1.dp, if (category == "오늘의 추천 만화") Color(0xFFF9B912) else Color(0xFFA5A5A5), RoundedCornerShape(0.dp))
            ) {
                Text(
                    text = "오늘의 추천 만화",
                    color = if (category == "오늘의 추천 만화") Color(0xFF000000) else Color(0xFFA5A5A5),
                    fontSize = 14.sp
                )
            }
        }


        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = 
            if (category == "사용자 취향 만화") userString
            else if (category == "베스트 셀러 만화") bestString 
            else todayString,
            fontSize = 20.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 만화 추천 리스트 뷰
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(top=10.dp,start=10.dp,end=10.dp)
                .background(Color.Transparent)
        ){
            // 카테고리에 따라 만화 리스트가 달라져야 하므로 여기서에서 fetch 가 실행되어야 할듯 ?

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                // 동적으로 아이템을 추가해서 보여줌
                items(cartoons) { cartoon ->
                    cartoon(cartoon, viewModel)
                }
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        if (clickedCartoon.title != "") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(250.dp)
                    .height(40.dp)
                    .background(Color(0xFFF9B912), RoundedCornerShape(8.dp))
                    .clickable(
                        // 밑에 두개 다 써야 클릭시 효과 제거 가능
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            viewModel.onClickedCartoonDetail()
                        }
                    )
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