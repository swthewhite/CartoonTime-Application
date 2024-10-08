package com.alltimes.cartoontime.ui.screen.moneytransaction.charge

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.composable.Loading
import com.alltimes.cartoontime.ui.screen.composable.Numpad
import com.alltimes.cartoontime.ui.viewmodel.ChargeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ChargePasswordInputScreen(viewModel: ChargeViewModel) {

    // viewmodel variable
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    val redirectUrl = viewModel.redirectUrl

    val startActivityForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("ChargePasswordInputScreen", "onActivityResult: ${result.resultCode}")
        Log.d("ChargePasswordInputScreen", "onActivityResult: OK: ${Activity.RESULT_OK}")
        Log.d("ChargePasswordInputScreen", "onActivityResult: CANCELED: ${Activity.RESULT_CANCELED}")
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.handlePaymentResult()

            if (result.resultCode == Activity.RESULT_OK) {
                // 결과 처리
                val data = result.data
                // 원하는 데이터 처리 (예: 결제 성공 후의 데이터)
                Log.d("ChargePasswordInputScreen", "onActivityResult: ${data?.extras}")
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // 결과가 취소된 경우 처리
//                CoroutineScope(Dispatchers.Main).launch {
//                    viewModel.handlePaymentError("결제 취소")
//                }
                Log.d("ChargePasswordInputScreen", "결제 취소")
            } else {
                // 결과가 실패한 경우 처리
//                CoroutineScope(Dispatchers.Main).launch {
//                    viewModel.handlePaymentError("결제 실패")
//                }
                Log.d("ChargePasswordInputScreen", "결제 실패")
            }
        }
    }



    // screen variable
    val imgSize = 40.dp
    val imgSpace = 10.dp

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
    ) {
        val (backButton, title, description0, description1, passwordRow, description2, numPad) = createRefs()

        // 뒤로가기 버튼
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .background(Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.goScreen(ScreenType.CHARGEPOINTINPUT)
                        viewModel.initializePassword()
                    }
                )
                .constrainAs(backButton) {
                    top.linkTo(parent.top, margin = 10.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back Icon",
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
            )
        }

        // 상단 타이틀
        Text(
            text = "포인트 충전하기",
            fontSize = 30.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(backButton.top)
                bottom.linkTo(backButton.bottom)
                start.linkTo(parent.start, margin = 10.dp)
                end.linkTo(parent.end, margin = 10.dp)
                width = Dimension.wrapContent
            }
        )

        Text(
            text = "간편 비밀번호",
            fontSize = 36.sp,
            color = Color.Black,
            modifier = Modifier
                .constrainAs(description0) {
                    top.linkTo(title.bottom, margin = 50.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "비밀번호를 입력해주세요.",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier
                .constrainAs(description1) {
                    top.linkTo(description0.bottom, margin = 50.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )


        // 비밀번호 6자리 표시
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp, end = 50.dp)
                .constrainAs(passwordRow) {
                    top.linkTo(description1.bottom, margin = 50.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(6) { i ->
                Image(
                    painter = if (password.length > i)
                        painterResource(id = R.drawable.ic_filled_circle)
                    else painterResource(id = R.drawable.ic_empty_circle),
                    contentDescription = "password${i + 1}",
                    modifier = Modifier
                        .size(imgSize)
                        .padding(horizontal = imgSpace)
                )
            }
        }

        // 숫자패드
        // 0 ~ 9, 삭제 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(numPad) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Numpad(viewModel)
        }
    }

    // 웹뷰로 카카오페이 결제 페이지 열기
    // 리다이렉트 url과 함께 콜백이 설정되어있을테니 성공,실패 시 서버에서 처리 가능
    redirectUrl.value?.let { url ->
        Log.d("ChargePasswordInputScreen", "Loading URL: $url")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivityForResult.launch(intent)
    }

    // 로딩 다이얼로그
    isLoading?.let { Loading("충전 중...", isLoading = it, onDismiss = { /* Dismiss Logic */ }) }

    // 인터넷 로딩 다이얼로그 표시
    networkStatus?.let { Loading("인터넷 연결 시도중 ... ", isLoading = it, onDismiss = { /* Dismiss Logic */ }) }

}