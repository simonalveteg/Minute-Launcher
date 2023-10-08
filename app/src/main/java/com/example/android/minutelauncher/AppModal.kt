package com.example.android.minutelauncher

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.minutelauncher.db.App


@Composable
fun AppModal(
  app: App,
  onEvent: (Event) -> Unit,
  onConfirmation: () -> Unit,
  onDismiss: () -> Unit,
  viewModel: LauncherViewModel = hiltViewModel(),
) {
  val mContext = LocalContext.current
  val appUsage = viewModel.getUsageForApp(app).longValue
  val favoriteApps by viewModel.favoriteApps.collectAsState(initial = emptyList())
  val isFavorite = favoriteApps.any { it.app.packageName == app.packageName }
  val favoriteIcon = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder

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
          Icon(imageVector = favoriteIcon, contentDescription = "Favorite")
        }
        Text(
          text = app.appTitle, style = MaterialTheme.typography.h5
        )
        IconButton(onClick = {
          val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", app.packageName, null)
          }
          startActivity(mContext, intent, null)
        }) {
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