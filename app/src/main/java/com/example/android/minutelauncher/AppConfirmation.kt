package com.example.android.minutelauncher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppConfirmation(
  app: UserApp,
  viewModel: LauncherViewModel = hiltViewModel(),
  onConfirmation: () -> Unit,
  onDismiss: () -> Unit
) {

  val appUsage = viewModel.getUsageForApp(app).value

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.4f),
      shape = RoundedCornerShape(10.dp)
    ) {
      Column(
        modifier = Modifier.padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = "${app.appTitle} used for ${appUsage.toTimeUsed(false)}")
        Row(
          modifier = Modifier.fillMaxSize().padding(bottom = 8.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.Bottom
        ) {
          TextButton(onClick = onConfirmation) {
            Text(text = "Open anyway")
          }
          Button(onClick = onDismiss) {
            Text(text = "Cancel")
          }
        }
      }
    }
  }
}