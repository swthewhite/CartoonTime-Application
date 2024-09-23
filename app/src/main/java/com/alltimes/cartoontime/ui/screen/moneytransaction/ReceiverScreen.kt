package com.alltimes.cartoontime.ui.screen.moneytransaction

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alltimes.cartoontime.ui.viewmodel.ReceiveViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(viewModel: ReceiveViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Receive Activity") })
        },
        content = {
            Button(
                onClick = {
                    viewModel.onButtonClick()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (uiState.isRunning) "Server Off" else "Server On",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
}