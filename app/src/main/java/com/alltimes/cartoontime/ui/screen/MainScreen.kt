package com.alltimes.cartoontime.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alltimes.cartoontime.data.model.ActivityType
import com.alltimes.cartoontime.ui.view.ReceiveActivity
import com.alltimes.cartoontime.ui.view.SendActivity
import com.alltimes.cartoontime.ui.view.SignUpActivity
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { viewModel.onSendButtonClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Send")
        }
        Button(
            onClick = { viewModel.onReceiveButtonClick() },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Receive")
        }
    }
}