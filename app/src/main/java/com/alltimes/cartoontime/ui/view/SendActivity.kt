package com.alltimes.cartoontime.ui.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.alltimes.cartoontime.ui.theme.CartoonTimeTheme
import com.alltimes.cartoontime.ui.viewmodel.SendViewModel

class SendActivity : ComponentActivity() {

    private lateinit var viewModel: SendViewModel
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(SendViewModel::class.java)

        // 권한 요청을 위한 ActivityResultLauncher 초기화
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                    permissions[Manifest.permission.BLUETOOTH_CONNECT] == true

            if (allGranted) {
                viewModel.startScanningAndConnect()  // 권한이 허용된 경우 BLE 스캔 시작
            } else {
                // 권한이 거부된 경우 처리
            }
        }

        // 권한 확인 및 요청
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        }

        setContent {
            CartoonTimeTheme {
                SendScreen(viewModel)
            }
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestBluetoothPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        )
    }

    companion object {
        private const val REQUEST_CODE_BLUETOOTH = 1001
    }
}

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
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
