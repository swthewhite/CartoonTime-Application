package com.alltimes.cartoontime.ui.screen.composable

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
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

    // 이미지 로드 상태를 추적하는 변수
    var isLoading by remember { mutableStateOf(true) } // 처음엔 로딩 중으로 설정


    LaunchedEffect(url) {
        if (url.isNotEmpty()) {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        bitmapState = resource.asImageBitmap() // 성공적으로 이미지를 로드
                        isLoading = false // 로딩 완료
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        bitmapState = blackBitmap.asImageBitmap() // 이미지 로딩 실패 시 검은색 이미지 사용
                        isLoading = false // 로딩 완료
                    }
                })
        } else {
            bitmapState = blackBitmap.asImageBitmap() // URL이 비어있을 경우 검은색 이미지 사용
            isLoading = false // 로딩 완료
        }
    }

    Box(
        contentAlignment = Alignment.Center, // 중앙 정렬
        modifier = Modifier
            .run {
                if (width > 0.dp) {
                    this.width(width) // width가 양수일 경우 설정
                } else {
                    this.fillMaxWidth() // 그렇지 않으면 fillMaxWidth
                }
            }
            .height(height) // height는 항상 설정
    ) {
        // 이미지가 로드되면 보여주고, 로드 중일 때는 커스텀 애니메이션을 보여줌
        if (isLoading) {
            LoadingAnimation() // 로딩 애니메이션 표시
        } else {
            // 이미지가 로드되었을 때 보여주기
            bitmapState?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .run {
                            if (width > 0.dp) {
                                this.width(width) // width 설정
                            } else {
                                this.fillMaxWidth() // fillMaxWidth 설정
                            }
                        }
                        .height(height) // height 설정
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop // 비율에 맞춰 크롭
                )
            }
        }
    }
}