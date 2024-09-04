package com.alltimes.cartoontime.ui.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.alltimes.cartoontime.ui.theme.CartoonTimeTheme

class SendActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartoonTimeTheme {
                SendScreen()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Send Activity") })
        }
    ) {
        Text("Send Activity Content", style = MaterialTheme.typography.headlineLarge)
    }
}