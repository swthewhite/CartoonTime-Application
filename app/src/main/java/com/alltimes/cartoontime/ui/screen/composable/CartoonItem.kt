package com.alltimes.cartoontime.ui.screen.composable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import com.alltimes.cartoontime.data.remote.ComicResponse
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

@Composable
fun CartoonItem(comic: ComicResponse, viewModel: MainViewModel) {

    // 버튼 클릭 상태를 관리할 변수
    val clickedComic by viewModel.clickedCartoon.collectAsState()

    // 동적 구성 배치 요소
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(250.dp)
            .border(
                if (clickedComic?.id == comic.id) 3.dp else 1.dp,
                if (clickedComic?.id == comic.id) Color(0xFFF9B912) else Color.Black,
                RoundedCornerShape(8.dp)
            )
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    viewModel.onClickedCartoon(comic)
                }
            )
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            // 만화 표지
            GlideImage(url = comic.imageUrl, -1.dp, 180.dp)

            Spacer(modifier = Modifier.height(20.dp))

            // 만화 제목
            Text(
                text = comic.titleKo,
                fontSize = 16.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
