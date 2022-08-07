package com.example.android.minutelauncher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun AppInfo(
  onFavorite: () -> Unit, onHide: () -> Unit, onUninstall: () -> Unit, onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .padding(32.dp)
        .fillMaxWidth(), shape = RoundedCornerShape(10.dp)
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = "App Info", style = MaterialTheme.typography.h5
        )
        TextButton(onClick = {
          onFavorite()
          onDismiss()
        }) {
          Text("Favorite")
          TextButton(onClick = {
            onHide()
            onDismiss()
          }) {
            Text("Hide")
          }
          TextButton(onClick = {
            onUninstall()
            onDismiss()
          }) {
            Text("Uninstall")
          }
        }
      }
    }
  }
}