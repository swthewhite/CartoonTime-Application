package com.alltimes.cartoontime.ui.screen.boot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.viewmodel.BootViewModel
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel

@Composable
fun LoginScreen(viewModel: BootViewModel) {
    val password by viewModel.password.collectAsState()

    val imgSize = 40.dp
    val imgSpace = 10.dp
    val btnSpace = 80.dp

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (title, passwordRow, numberPad) = createRefs()

        // 간편 비밀번호 설정 타이틀
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp)
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "간편 비밀번호",
                fontSize = 36.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = "비밀번호를 입력하세요",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        // 비밀번호 6자리 표시
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
                .constrainAs(passwordRow) {
                    top.linkTo(title.bottom, margin = 36.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 1..6) {
                Image(
                    painter = if (password.length >= i)
                        painterResource(id = R.drawable.ic_filled_circle)
                    else painterResource(id = R.drawable.ic_empty_circle),
                    contentDescription = "password$i",
                    modifier = Modifier
                        .size(imgSize)
                        .padding(horizontal = imgSpace)
                )
            }
        }

        // 숫자 버튼들
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(numberPad) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            // 숫자 버튼들
            NumberRow(viewModel, listOf(1, 2, 3))
            NumberRow(viewModel, listOf(4, 5, 6))
            NumberRow(viewModel, listOf(7, 8, 9))

            // 마지막 줄 (빈칸, 0, 삭제)
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { /* 빈칸 */ },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier.weight(1f).heightIn(min = 60.dp)
                ) {
                    Text(" ", color = Color(0xFF000000), fontSize = 40.sp)
                }

                Button(
                    onClick = { viewModel.onClickedButton(0) },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier.weight(1f).heightIn(min = 60.dp)
                ) {
                    Text("0", color = Color(0xFF000000), fontSize = 40.sp)
                }

                Button(
                    onClick = { viewModel.onClickedButton(-1) },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                    modifier = Modifier.weight(1f).heightIn(min = 60.dp)
                ) {
                    Text("<-", color = Color(0xFF000000), fontSize = 40.sp)
                }
            }
        }
    }
}

@Composable
fun NumberRow(viewModel: BootViewModel, numbers: List<Int>) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        numbers.forEach { number ->
            Button(
                onClick = { viewModel.onClickedButton(number) },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 60.dp)
            ) {
                Text(
                    text = number.toString(),
                    color = Color(0xFF000000),
                    fontSize = 40.sp
                )
            }
        }
    }
}