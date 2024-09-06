package com.alltimes.cartoontime.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.viewmodel.BootViewModel

@Composable
fun BootScreen(viewModel: BootViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF9B912))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.title_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(300.dp)
        )

        Spacer(modifier = Modifier.height(150.dp))

        Button(
            onClick = { viewModel.onLoginClick() },
            colors = ButtonDefaults.buttonColors(Color(0xFF3C2C10)),
            shape = RoundedCornerShape(15),
            modifier = Modifier
                .size(width = 300.dp, height = 50.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text("로그인하기")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "아직 회원이 아니신가요?",
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { viewModel.onSignUpClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = "회원가입",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}
