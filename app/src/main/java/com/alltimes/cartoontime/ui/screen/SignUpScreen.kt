package com.alltimes.cartoontime.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(viewModel: SignUpViewModel) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val name by viewModel.name.collectAsState()
    val isVerificationCodeVisible by viewModel.isVerificationCodeVisible.collectAsState()
    val isSubmitButtonEnabled by viewModel.isSubmitButtonEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 상단: 전화번호 입력 및 인증번호 받기 버튼
        Column {
            TextField(
                value = phoneNumber,
                onValueChange = { viewModel.onPhoneNumberChange(it) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color.Gray, shape = MaterialTheme.shapes.small)
                    .padding(16.dp),
                label = { Text("전화번호") }
            )
            Button(
                onClick = { viewModel.onRequestVerificationCode() },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("인증번호 받기")
            }
        }

        // 중간: 인증번호 입력
        if (isVerificationCodeVisible) {
            TextField(
                value = verificationCode,
                onValueChange = { viewModel.onVerificationCodeChange(it) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color.Gray, shape = MaterialTheme.shapes.small)
                    .padding(16.dp),
                label = { Text("인증번호") }
            )
        }

        // 하단: 이름 입력 및 인증하기 버튼
        Column {
            TextField(
                value = name,
                onValueChange = { viewModel.onNameChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color.Gray, shape = MaterialTheme.shapes.small)
                    .padding(16.dp),
                label = { Text("이름") }
            )
            Button(
                onClick = { viewModel.onSubmit() },
                enabled = isSubmitButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("인증하기")
            }
        }
    }
}
