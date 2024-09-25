package com.alltimes.cartoontime.ui.screen.moneytransaction.send

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.ui.viewmodel.SendViewModel

@Composable
fun SendConfirmScreen(viewModel: SendViewModel) {

    val point by viewModel.point.collectAsState()
    val toUserName by viewModel.toUserName.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (title, checkImage, description0, description1, description2, btnConfirm) = createRefs()


        // 상단 타이틀
        Text(
            text = "Send Witch",
            fontSize = 30.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top, margin = 20.dp)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
                width = Dimension.wrapContent
            }
        )

        Image(
            painter = painterResource(id = R.drawable.ic_check),
            contentDescription = "Check Icon",
            modifier = Modifier
                .constrainAs(checkImage) {
                    top.linkTo(title.bottom, margin = 250.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .width(50.dp)
                .height(50.dp)
        )

        Text(
            text = "${toUserName}님 지갑으로",
            fontSize = 20.sp,
            modifier = Modifier
                .constrainAs(description0) {
                    top.linkTo(checkImage.bottom, margin = 50.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                }
        )

        Text(
            text = "${point} 포인트를",
            fontSize = 20.sp,
            modifier = Modifier
                .constrainAs(description1) {
                    top.linkTo(description0.bottom, margin = 5.dp)
                    start.linkTo(description0.start)
                    end.linkTo(description0.end)
                }
        )

        Text(
            text = "보냈어요.",
            fontSize = 20.sp,
            modifier = Modifier
                .constrainAs(description2) {
                    top.linkTo(description1.bottom, margin = 5.dp)
                    start.linkTo(description1.start)
                    end.linkTo(description1.end)
                }
        )

        Button(
            onClick = {
                viewModel.goActivity(ActivityType.MAIN)
            },
            colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
            modifier = Modifier
                .width(350.dp)
                .height(50.dp)

                .constrainAs(btnConfirm) {
                    bottom.linkTo(parent.bottom, margin = 15.dp)
                    start.linkTo(parent.start, margin = 15.dp)
                    end.linkTo(parent.end, margin = 15.dp)
                }
        ) {
            Text(
                text = "확인",
                fontSize = 20.sp,
                color = Color.Black
            )
        }
    }
}