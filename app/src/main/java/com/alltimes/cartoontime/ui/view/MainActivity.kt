package com.alltimes.cartoontime.ui.view

// # Added Imports
// ## Manifest Import
import android.Manifest
// ## PackageManger Import
import android.content.pm.PackageManager
// ## Build Import
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// Origin Imports

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.alltimes.cartoontime.data.model.ActionType
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import com.alltimes.cartoontime.ui.theme.CartoonTimeTheme

class MainActivity : ComponentActivity() {
    private val allPermission =
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )

    private fun haveAllPermissions(): Boolean {
        return allPermission
            .all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun requestMissingPermissions() {
        if (!haveAllPermissions()) {
            ActivityCompat.requestPermissions(this, allPermission, 1)
        }
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestMissingPermissions()

        setContent {
            CartoonTimeTheme {
                viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val action by viewModel.action.observeAsState()
    val context = LocalContext.current

    // Layout for the main screen with two buttons
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

    // action을 감지하여 내비게이션 처리
    LaunchedEffect(action) {
        action?.actionType?.let { actionType ->
            navigateToActivity(actionType, context)
        }
    }
}

// 내비게이션 함수는 @Composable이 아닌 일반 함수로 정의
private fun navigateToActivity(actionType: ActionType, context: Context) {
    val intent = when (actionType) {
        ActionType.SEND -> Intent(context, SendActivity::class.java)
        ActionType.RECEIVE -> Intent(context, ReceiveActivity::class.java)
    }
    context.startActivity(intent)
}
