package com.alltimes.cartoontime.ui.screen.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alltimes.cartoontime.common.NumpadAction

@Composable
fun Loading(isLoading: Boolean, onDismiss: () -> Unit) {
    if (isLoading) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = null,
            containerColor = Color(0xFFFAFAFA),
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color(0xFFF4F2EE), shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally, // 가로 가운데 정렬
                        verticalArrangement = Arrangement.Center,
                    ){
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp) // 스피너 크기 조정
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "인터넷 연결 시도 중 ..."
                        )
                    }
                }
            },
            confirmButton = {
                // 확인 버튼 필요 시 추가
                // 스피너 대신 설명 텍스트를 넣을 수 있음
            },
            dismissButton = null // 필요에 따라 버튼 추가 가능
        )
    }
}