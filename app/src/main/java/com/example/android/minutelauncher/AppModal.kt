package com.example.android.minutelauncher

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun AppModal(
  app: UserApp,
  onEvent: (Event) -> Unit,
  onConfirmation: () -> Unit,
  onDismiss: () -> Unit,
  viewModel: LauncherViewModel = hiltViewModel(),
) {
  val appUsage = viewModel.getUsageForApp(app).value
  val mContext = LocalContext.current

  Surface {
    Column(
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = {
          onEvent(Event.ToggleFavorite(app))
        }) {
          Icon(imageVector = Icons.Default.Star, contentDescription = "Favorite")
        }
        Text(
          text = app.appTitle, style = MaterialTheme.typography.h5
        )
        IconButton(onClick = {}) {
          Icon(imageVector = Icons.Default.Info, contentDescription = "App Info")
        }
      }
      Text(
        text = "${app.appTitle} used for ${appUsage.toTimeUsed(false)}",
        modifier = Modifier.padding(64.dp)
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 22.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
      ) {
        TextButton(onClick = onConfirmation) {
          Text(text = "Open anyway")
        }
        Button(onClick = onDismiss) {
          Text(text = "Put the phone down")
        }
      }
    }
  }
}