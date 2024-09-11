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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel

@Composable
fun cartoon(cartoon: Cartoon, viewModel: MainViewModel) {

    // 버튼 클릭 상태를 관리할 변수
    val clickedCartoon by viewModel.clickedCartoon.collectAsState()

    // 동적 구성 배치 요소
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(250.dp)
            .border(
                if (clickedCartoon.title == cartoon.title) 3.dp else 1.dp,
                if (clickedCartoon.title == cartoon.title) Color(0xFFF9B912) else Color.Black, // 같은 경우 F9B912로 변경
                RoundedCornerShape(8.dp)
            )
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable(
                // 밑에 두개 다 써야 클릭시 효과 제거 가능
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.onClickedCartoon(cartoon)
                }
            ) // 버튼처럼 동작하도록 clickable 사용
            .padding(8.dp) // padding을 적절히 추가
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            // 만화 표지
            Image(
                painter = painterResource(id = R.drawable.image_book), //rememberImagePainter(cartoon.coverUrl),
                contentDescription = cartoon.title,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 만화 제목
            Text(
                text = cartoon.title,
                fontSize = 16.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}