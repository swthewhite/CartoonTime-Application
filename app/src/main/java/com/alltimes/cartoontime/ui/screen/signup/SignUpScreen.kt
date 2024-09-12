package com.alltimes.cartoontime.ui.screen.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(viewModel: SignUpViewModel) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val name by viewModel.name.collectAsState()
    val isVerificationCodeVisible by viewModel.isVerificationCodeVisible.collectAsState()
    val isSubmitButtonEnabled by viewModel.isSubmitButtonEnabled.collectAsState()
    val isNameCorrect by viewModel.isNameCorrect.collectAsState()
    val isPhoneNumberEnable by viewModel.isPhoneNumberEnable.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
            .padding(16.dp)
    ) {
        // References for the components
        val (exitButton, titleText, instructionText, phoneText, phoneField, verifyCodeField, requestButton, codeRequestButton, plusTimeButton, nameText, nameField, submitButton) = createRefs()

        // Exit button
        IconButton(
            onClick = { viewModel.onLogout() },
            modifier = Modifier
                .size(24.dp)
                .constrainAs(exitButton) {
                    top.linkTo(parent.top, margin = 5.dp)
                    end.linkTo(parent.end, margin = 5.dp)
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_exit),
                contentDescription = "Exit",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title text
        Text(
            text = "휴대폰 본인인증",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(titleText) {
                top.linkTo(exitButton.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 5.dp)
            }
        )

        // Instruction text
        Text(
            text = "인증번호를 요청해주세요.",
            fontSize = 24.sp,
            modifier = Modifier.constrainAs(instructionText) {
                top.linkTo(titleText.bottom, margin = 10.dp)
                start.linkTo(parent.start, margin = 5.dp)
            }
        )

        // Phone Number Label
        Text(
            text = "휴대폰 번호",
            fontSize = 16.sp,
            modifier = Modifier.constrainAs(phoneText) {
                top.linkTo(instructionText.bottom, margin = 80.dp)
                start.linkTo(parent.start, margin = 5.dp)
            }
        )

        // Phone Number TextField
        TextField(
            enabled = isPhoneNumberEnable,
            value = phoneNumber,
            onValueChange = { viewModel.onPhoneNumberChange(it) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent, // 비활성 상태에서 밑줄 색상 제거
                cursorColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                )
                .constrainAs(phoneField) {
                    top.linkTo(phoneText.bottom, margin = 3.dp)
                    start.linkTo(parent.start, margin = 5.dp)
                    end.linkTo(parent.end, margin = 5.dp)
                },
            label = {
                Text(text = "- 없이 입력", color = Color.Black)
            }
        )

        // 인증번호 요청 버튼
        if (!isVerificationCodeVisible) {
            Button(
                onClick = { viewModel.onRequestVerificationCode() },
                shape = RoundedCornerShape(15),
                colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                modifier = Modifier
                    .constrainAs(requestButton) {
                        top.linkTo(phoneField.bottom, margin = 10.dp)
                        end.linkTo(parent.end, margin = 5.dp)
                    }
            ) {
                Text(text = "인증번호 요청", color = Color(0xFF606060))
            }
        }

        // 인증번호 입력 관련
        if (isVerificationCodeVisible) {
            TextField(
                value = verificationCode,
                onValueChange = { viewModel.onVerificationCodeChange(it) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .constrainAs(verifyCodeField) {
                        top.linkTo(phoneField.bottom, margin = 10.dp)
                        start.linkTo(parent.start, margin = 5.dp)
                        end.linkTo(parent.end, margin = 5.dp)
                    },
                label = {
                    Text(text = "인증번호", color = Color.Black)
                }
            )

            if (isVerificationCodeVisible) {
                Button(
                    onClick = { println("시간 연장") },
                    shape = RoundedCornerShape(15),
                    colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                    modifier = Modifier
                        .padding(2.dp)
                        .constrainAs(plusTimeButton) {
                            top.linkTo(verifyCodeField.bottom, margin = 3.dp)
                            end.linkTo(parent.end, margin = 5.dp)
                        }
                ) {
                    Text(
                        text = "시간 연장",
                        color = Color(0xFF606060),
                    )
                }

                Button(
                    onClick = { println("인증번호 재요청") },
                    shape = RoundedCornerShape(15),
                    colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                    modifier = Modifier
                        .padding(2.dp)
                        .constrainAs(codeRequestButton) {
                            top.linkTo(verifyCodeField.bottom, margin = 3.dp)
                            end.linkTo(plusTimeButton.start, margin = 5.dp)
                        }
                ) {
                    Text(
                        text = "인증번호 재요청",
                        color = Color(0xFF606060),
                    )
                }
            }


            // Name Label
            Text(
                text = "이름",
                fontSize = 16.sp,
                modifier = Modifier.constrainAs(nameText) {
                    top.linkTo(codeRequestButton.bottom, margin = 100.dp)
                    start.linkTo(parent.start, margin = 5.dp)
                }
            )

            // Name TextField
            TextField(
                value = name,
                onValueChange = { viewModel.onNameChange(it) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .constrainAs(nameField) {
                        top.linkTo(nameText.bottom, margin = 3.dp)
                        start.linkTo(parent.start, margin = 5.dp)
                        end.linkTo(parent.end, margin = 5.dp)
                    },
                label = {
                    Text(text = "이름", color = Color.Black)
                }
            )

            // Submit Button
            Button(
                onClick = { viewModel.onSubmit() },
                shape = RoundedCornerShape(15),
                enabled = isSubmitButtonEnabled,
                colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(submitButton) {
                        top.linkTo(nameField.bottom, margin = 50.dp)
                        start.linkTo(parent.start, margin = 5.dp)
                        end.linkTo(parent.end, margin = 5.dp)
                        bottom.linkTo(parent.bottom, margin = 5.dp)
                    }
            ) {
                Text(text = "인증하기", color = Color(0xFF606060))
            }
        }
    }
}