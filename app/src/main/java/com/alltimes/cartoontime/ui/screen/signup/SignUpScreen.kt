package com.alltimes.cartoontime.ui.screen.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.ui.screen.composable.Loading
import com.alltimes.cartoontime.ui.screen.composable.LoadingAnimation
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(viewModel: SignUpViewModel) {

    // viewmodel variable
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val name by viewModel.name.collectAsState()
    val isVerificationCodeVisible by viewModel.isVerificationCodeVisible.collectAsState()
    val isSubmitButtonEnabled by viewModel.isSubmitButtonEnabled.collectAsState()
    val isNameEnable by viewModel.isNameEnable.collectAsState()
    val isPhoneNumberEnable by viewModel.isPhoneNumberEnable.collectAsState()
    val isverificationCodeCorrect by viewModel.isVerificationCodeCorrect.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState() // isLoading 변수를 뷰모델에서 관리


    // screen variable
    val keyboardController = LocalSoftwareKeyboardController.current

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
            .padding(16.dp)
    ) {
        val (exitButton, titleText, instructionText, phoneText, phoneField, verifyCodeField, requestButton, verficationButton, nameText, nameField, submitButton) = createRefs()

        IconButton(
            onClick = { viewModel.goActivity(ActivityType.FINISH) },
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

        Text(
            text = "휴대폰 본인인증",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(titleText) {
                top.linkTo(exitButton.bottom, margin = 20.dp)
                start.linkTo(parent.start, margin = 5.dp)
            }
        )

        Text(
            text = "인증번호를 요청해주세요.",
            fontSize = 24.sp,
            modifier = Modifier.constrainAs(instructionText) {
                top.linkTo(titleText.bottom, margin = 10.dp)
                start.linkTo(parent.start, margin = 5.dp)
            }
        )

        Text(
            text = "휴대폰 번호",
            fontSize = 16.sp,
            modifier = Modifier.constrainAs(phoneText) {
                top.linkTo(instructionText.bottom, margin = 80.dp)
                start.linkTo(parent.start, margin = 5.dp)
            }
        )

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
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next // 다음 필드로 이동
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    keyboardController?.hide() // 키패드 숨기기
                }
            )
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
                enabled = !isverificationCodeCorrect,
                onValueChange = { viewModel.onVerificationCodeChange(it) },
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
                    .constrainAs(verifyCodeField) {
                        top.linkTo(phoneField.bottom, margin = 10.dp)
                        start.linkTo(parent.start, margin = 5.dp)
                        end.linkTo(parent.end, margin = 5.dp)
                    },
                label = {
                    Text(text = "인증번호", color = Color.Black)
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next // 다음 필드로 이동
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        keyboardController?.hide() // 키패드 숨기기
                    }
                )
            )

            if (isVerificationCodeVisible) {
                Button(
                    onClick = { viewModel.onVerify() },
                    shape = RoundedCornerShape(15),
                    colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                    modifier = Modifier
                        .padding(2.dp)
                        .constrainAs(verficationButton) {
                            top.linkTo(verifyCodeField.bottom, margin = 3.dp)
                            end.linkTo(parent.end, margin = 5.dp)
                        }
                ) {
                    Text(
                        text = "인증하기",
                        color = Color(0xFF606060),
                    )
                }
            }


            if (isverificationCodeCorrect) {
                // 회원 정보가 있는 경우에는 이름 입력불가
                // 없는 경우에는 이름 입력
                Text(
                    text = "이름",
                    fontSize = 16.sp,
                    modifier = Modifier.constrainAs(nameText) {
                        top.linkTo(verficationButton.bottom, margin = 100.dp)
                        start.linkTo(parent.start, margin = 5.dp)
                    }
                )

                // Name TextField
                TextField(
                    value = name,
                    enabled = isNameEnable,
                    onValueChange = { viewModel.onNameChange(it) },
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
                        .constrainAs(nameField) {
                            top.linkTo(nameText.bottom, margin = 3.dp)
                            start.linkTo(parent.start, margin = 5.dp)
                            end.linkTo(parent.end, margin = 5.dp)
                        },
                    label = {
                        Text(text = "이름", color = Color.Black)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next // 다음 필드로 이동
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            keyboardController?.hide() // 키패드 숨기기
                        }
                    )
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
                    Text(text = "등록하기", color = Color(0xFF606060))
                }
            }
        }
    }

    // 로딩 다이얼로그
    if (isLoading) {
        Dialog(onDismissRequest = {  }) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // 애니메이션을 추가할 수 있는 부분입니다.
                // 여기서는 단순히 로딩 텍스트와 애니메이션을 보여줍니다.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LoadingAnimation() // 로딩 인디케이터
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "등록 중...", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 로딩 다이얼로그 표시
    networkStatus?.let { Loading(isLoading = it, onDismiss = { /* Dismiss Logic */ }) }
}