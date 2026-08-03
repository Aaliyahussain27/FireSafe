package com.example.firesafe.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MainScreen(data: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        data.forEach { item ->
            Text(text = "Hello $item!")
        }
    }
}
