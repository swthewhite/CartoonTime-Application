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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.composable.Map
import com.alltimes.cartoontime.ui.screen.composable.cartoon
import kotlinx.coroutines.delay

@Composable
fun BookDetailScreen(viewModel: MainViewModel) {
    val clickedCartoon by viewModel.clickedCartoon.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (backButton, bookImage, title, author, genre, locationText, map) = createRefs()

        // 상단 바 메뉴
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .background(Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.goScreen(ScreenType.BOOKRECOMMEND)
                    }
                )
                .constrainAs(backButton) {
                    top.linkTo(parent.top, margin = 10.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back Icon",
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.image_book),
            contentDescription = "Book Image",
            modifier = Modifier
                .width(200.dp)
                .height(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .constrainAs(bookImage) {
                    top.linkTo(backButton.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                },
            contentScale = ContentScale.Crop
        )

        Text(
            text = clickedCartoon.title,
            fontSize = 24.sp,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(bookImage.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Text(
            text = clickedCartoon.author,
            fontSize = 12.sp,
            modifier = Modifier.constrainAs(author) {
                top.linkTo(title.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Text(
            text = clickedCartoon.genre,
            fontSize = 12.sp,
            modifier = Modifier.constrainAs(genre) {
                top.linkTo(author.bottom, margin = 10.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Text(
            text = "도서 위치  " + clickedCartoon.sector + "  구역",
            fontSize = 20.sp,
            modifier = Modifier.constrainAs(locationText) {
                top.linkTo(genre.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(10.dp)
                .background(Color.Transparent)
                .constrainAs(map) {
                    top.linkTo(locationText.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            Map(viewModel)
        }
    }
}