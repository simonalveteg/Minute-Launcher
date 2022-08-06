package com.example.android.minutelauncher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen() {
  Surface {
    Column(Modifier.fillMaxSize()) {
      Text(text = "Gestures")
      Text(text = "Upper")
      Text(text = "Lower")
      Text(text = "Restart App")
    }
  }
}