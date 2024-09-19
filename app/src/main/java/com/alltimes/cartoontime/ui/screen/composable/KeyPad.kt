package com.alltimes.cartoontime.ui.screen.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.common.NumpadAction
import com.alltimes.cartoontime.common.PointpadAction

@Composable
fun Numpad(viewModel: NumpadAction) {

    val btnSpace = 80.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 숫자 버튼들
        (1..9 step 3).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                (row until row + 3).forEach { num ->
                    Button(
                        onClick = { viewModel.onClickedButton(num) },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                        modifier = Modifier
                            .weight(1f)
                            .height(btnSpace)
                    ) {
                        Text(
                            text = num.toString(),
                            color = Color(0xFF000000),
                            fontSize = 40.sp
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { /* 빈칸 */ },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                modifier = Modifier
                    .weight(1f)
                    .height(btnSpace)
            ) {
                Text(" ", color = Color(0xFF000000), fontSize = 40.sp)
            }
            Button(
                onClick = { viewModel.onClickedButton(0) },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                modifier = Modifier
                    .weight(1f)
                    .height(btnSpace)
            ) {
                Text("0", color = Color(0xFF000000), fontSize = 40.sp)
            }
            Button(
                onClick = { viewModel.onClickedButton(-1) },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                modifier = Modifier
                    .weight(1f)
                    .height(btnSpace)
            ) {
                Text("<-", color = Color(0xFF000000), fontSize = 40.sp)
            }
        }
    }
}

@Composable
fun Pointpad(viewModel: PointpadAction) {

    val btnSpace = 80.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 숫자 버튼들
        (1..9 step 3).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                (row until row + 3).forEach { num ->
                    Button(
                        onClick = { viewModel.onPointClickedButton(num) },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                        modifier = Modifier
                            .weight(1f)
                            .height(btnSpace)
                    ) {
                        Text(
                            text = num.toString(),
                            color = Color(0xFF000000),
                            fontSize = 40.sp
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.onPointClickedButton(-2) },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                modifier = Modifier
                    .weight(1f)
                    .height(btnSpace)
            ) {
                Text("00", color = Color(0xFF000000), fontSize = 40.sp)
            }
            Button(
                onClick = { viewModel.onPointClickedButton(0) },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                modifier = Modifier
                    .weight(1f)
                    .height(btnSpace)
            ) {
                Text("0", color = Color(0xFF000000), fontSize = 40.sp)
            }
            Button(
                onClick = { viewModel.onPointClickedButton(-1) },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9B912)),
                modifier = Modifier
                    .weight(1f)
                    .height(btnSpace)
            ) {
                Text("<-", color = Color(0xFF000000), fontSize = 40.sp)
            }
        }
    }
}