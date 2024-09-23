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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.composable.Map
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel

@Composable
fun ConfirmScreen(viewModel: MainViewModel) {

    val balance by viewModel.balance.collectAsState()
    val charge by viewModel.charge.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (description, button) = createRefs()

        Text(
            text = if (balance < charge) "결제 금액이 ${charge - balance} 부족합니다.\n포인트를 충전해주세요." else "정상적으로 퇴실이 완료되었습니다.",
            fontSize = 26.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(description) {
                    top.linkTo(parent.top, margin = 20.dp)
                    bottom.linkTo(parent.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                    width = Dimension.wrapContent
                }
        )

        Button(
            onClick = {
                if (balance < charge) viewModel.goActivity(ActivityType.CHARGE)
                else viewModel.onConfirmButtonClick()
            },
            colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
            modifier = Modifier
                .width(350.dp)
                .height(50.dp)

                .constrainAs(button) {
                    bottom.linkTo(parent.bottom, margin = 10.dp)
                    start.linkTo(parent.start, margin = 15.dp)
                    end.linkTo(parent.end, margin = 15.dp)
                }
        ) {
            Text(
                text = if (balance < charge) "포인트 충전하러가기" else "확인",
                fontSize = 20.sp,
                color = Color.Black
            )
        }
    }
}