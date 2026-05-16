package com.alveteg.simon.minutelauncher.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
  modifier: Modifier = Modifier
) {
  OutlinedButton(
    onClick = onClick,
    colors = colors,
    modifier = modifier.fillMaxWidth(1f).requiredHeightIn(52.dp).padding(vertical = 4.dp),
    shape = MaterialTheme.shapes.small,
  ) {

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Start,
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(
        imageVector = Icons.Default.Info,
        contentDescription = null,
        modifier = Modifier.padding(end = 16.dp)
      )
      Column(
        modifier = Modifier.fillMaxWidth(),
      ) {
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
  }
}