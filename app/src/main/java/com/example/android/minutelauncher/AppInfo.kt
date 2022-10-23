package com.example.android.minutelauncher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppInfo(
  app: UserApp,
  onEvent: (Event) -> Unit,
  onDismiss: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(0.55f),
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(
      modifier = Modifier.padding(top = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = app.appTitle, style = MaterialTheme.typography.h5
      )
      TextButton(onClick = {
        onEvent(Event.ToggleFavorite(app))
        onDismiss()
      }) {
        Text("Favorite")
      }
      TextButton(onClick = {
        onEvent(Event.ToggleFavorite(app))
        onDismiss()
      }) {
        Text("Hide")
      }
      TextButton(onClick = {
        onEvent(Event.ToggleFavorite(app))
        onDismiss()
      }) {
        Text("App Info")
      }
      TextButton(onClick = {
        onEvent(Event.ToggleFavorite(app))
        onDismiss()
      }) {
        Text("Uninstall")
      }
    }
  }
}