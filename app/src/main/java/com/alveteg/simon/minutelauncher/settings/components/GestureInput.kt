package com.alveteg.simon.minutelauncher.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.settings.SettingsEvent
import com.alveteg.simon.minutelauncher.utilities.Gesture


@Composable
fun GestureInput(
  gesture: Gesture,
  app: App?,
  iconResource: Int,
  onEvent: (SettingsEvent) -> Unit
) {
  val appTitle = app?.appTitle ?: "No app selected"
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Icon(
      painter = painterResource(iconResource),
      contentDescription = null,
      modifier = Modifier.padding(start = 8.dp, end = 12.dp)
    )
    OutlinedButton(
      onClick = {
        onEvent(SettingsEvent.OpenGestureList(gesture))
      },
      modifier = Modifier.weight(1f),
      shape = MaterialTheme.shapes.medium
    ) {
      Text(text = appTitle)
    }
    IconButton(
      onClick = {
        onEvent(SettingsEvent.ClearAppGesture(gesture))
      },
      enabled = app != null,
      modifier = Modifier.wrapContentWidth()
    ) {
      Icon(imageVector = Icons.Default.Close, contentDescription = "Unset Gesture")
    }
  }
}