package com.alltimes.cartoontime.ui.screen.moneytransaction

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.alltimes.cartoontime.ui.viewmodel.SendViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(viewModel: SendViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Send Activity") })
        },
        content = {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                Text(
                    text = if (uiState.isScanning) "Scanning for devices..." else "Not scanning",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (uiState.foundDevices.isNotEmpty()) {
                    Text(
                        text = "Found Devices:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    LazyColumn {
                        items(uiState.foundDevices) { device ->
                            Text(
                                text = device.name ?: "Unknown Device",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                } else {
                    Text(text = "No devices found", style = MaterialTheme.typography.bodyMedium)
                }

                if (uiState.isDeviceConnected) {
                    Text(
                        text = "Device Connected",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    Text(
                        text = "No device connected",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.onSendButtonClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (uiState.isSending) "Stop Sending" else "Start Sending",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    )
}
