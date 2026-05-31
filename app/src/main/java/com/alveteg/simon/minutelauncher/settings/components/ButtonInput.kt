package com.alveteg.simon.minutelauncher.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonInput(
  label: String,
  description: String? = null,
  colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
  onClick: () -> Unit,
  showDismiss: Boolean = false,
  onDismiss: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Surface(
    shape = MaterialTheme.shapes.small,
    color = colors.containerColor,
    contentColor = colors.contentColor,
    modifier = modifier
      .fillMaxWidth()
      .requiredHeightIn(min = 52.dp)
      .padding(vertical = 4.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.height(IntrinsicSize.Min)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clickable(onClick = onClick)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          modifier = Modifier.padding(end = 16.dp)
        )
        Column {
          Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
          )
          if (description != null) {
            Text(
              text = description,
              style = MaterialTheme.typography.labelSmall
            )
          }
        }
      }

      if (showDismiss) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onDismiss)
            .padding(horizontal = 16.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss"
          )
        }
      }
    }
  }
}