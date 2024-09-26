package com.alltimes.cartoontime.ui.screen.boot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.ui.viewmodel.BootViewModel

@Composable
fun BootScreen(viewModel: BootViewModel) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF9B912))
            .padding(16.dp)
    ) {
        val (logo, loginButton) = createRefs()

        // 로고 이미지
        Image(
            painter = painterResource(id = R.drawable.logo_title),
            contentDescription = "Logo",
            modifier = Modifier
                .size(300.dp)
                .constrainAs(logo) {
                    top.linkTo(parent.top, margin = 150.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        // 로그인 버튼
        Button(
            onClick = { viewModel.goActivity(ActivityType.SIGNUP) },
            colors = ButtonDefaults.buttonColors(Color(0xFF3C2C10)),
            shape = RoundedCornerShape(15),
            modifier = Modifier
                .size(width = 300.dp, height = 50.dp)
                .constrainAs(loginButton) {
                    top.linkTo(logo.bottom, margin = 150.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = 50.dp)
                }
        ) {
            Text("로그인하기")
        }
    }
}