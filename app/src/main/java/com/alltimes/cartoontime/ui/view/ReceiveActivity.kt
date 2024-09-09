package com.alltimes.cartoontime.ui.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.alltimes.cartoontime.ui.theme.CartoonTimeTheme
import com.alltimes.cartoontime.ui.viewmodel.ReceiverViewModel

class ReceiveActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartoonTimeTheme {
                val viewModel = remember { ViewModelProvider(this).get(ReceiverViewModel::class.java) }
                ReceiveScreen(viewModel)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(viewModel: ReceiverViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Receive Activity") })
        },
        content = {
            Button(
                onClick = { viewModel.onButtonClick()
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