package com.alveteg.simon.minutelauncher.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ButtonInput(
  label: String,
  modifier: Modifier = Modifier,
  imageVector: ImageVector = Icons.Default.Info,
  description: String? = null,
  colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
  onClick: () -> Unit,
  showDismiss: Boolean = false,
  onDismiss: () -> Unit = {},
) {
  Surface(
    shape = MaterialTheme.shapes.small,
    color = colors.containerColor,
    contentColor = colors.contentColor,
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
          .weight(1f)
          .clickable(onClick = onClick)
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Icon(
          imageVector = imageVector,
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
              style = MaterialTheme.typography.labelSmall,
              modifier = Modifier.padding(top = 1.dp)
            )
          }
        }
      }

      if (showDismiss) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .clickable(onClick = onDismiss)
            .padding(horizontal = 16.dp)
        ) {
          Text(
            text = "Hide".uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}