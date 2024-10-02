package com.alltimes.cartoontime.ui.screen.composable

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

@Composable
fun GlideImage(url: String, width: Dp, height: Dp) {
    val context = LocalContext.current
    val imageView = remember { ImageView(context) }

    // 검은색 이미지를 위한 Bitmap 생성
    val blackBitmap = remember { Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
        eraseColor(android.graphics.Color.TRANSPARENT) // 검은색으로 채우기
    }}

    // 이미지 상태를 저장하기 위한 변수
    var bitmapState by remember { mutableStateOf(blackBitmap.asImageBitmap()) }

    LaunchedEffect(url) {
        if (url.isNotEmpty()) {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        bitmapState = resource.asImageBitmap() // 성공적으로 이미지를 로드
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        bitmapState = blackBitmap.asImageBitmap() // 이미지 로딩 실패 시 검은색 이미지 사용
                    }
                })
        } else {
            bitmapState = blackBitmap.asImageBitmap() // URL이 비어있을 경우 검은색 이미지 사용
        }
    }

    // Modifier 설정
    val imageModifier = Modifier
        .run {
            if (width > 0.dp) {
                this.width(width) // width가 양수일 경우 설정
            } else {
                this.fillMaxWidth() // 그렇지 않으면 fillMaxWidth
            }
        }
        .height(height) // height는 항상 설정
        .clip(RoundedCornerShape(8.dp)) // 필요시 모서리 둥글게

    Image(
        bitmap = bitmapState,
        contentDescription = null,
        modifier = imageModifier,
        contentScale = ContentScale.Crop // 비율에 맞춰 크롭
    )
}