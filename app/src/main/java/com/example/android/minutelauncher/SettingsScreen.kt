package com.example.android.minutelauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(
  onNavigate: (String) -> Unit
) {
  Surface {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Button(onClick = { onNavigate("main") }) {
        Text(text = "GO BACK")
      }
      Text(text = "Gestures")
      Text(text = "Upper")
      Text(text = "Lower")
      Text(text = "Restart App")
    }
  }
}