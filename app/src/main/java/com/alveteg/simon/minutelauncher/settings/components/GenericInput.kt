package com.alveteg.simon.minutelauncher.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GenericInput(
  label: String,
  description: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Column(modifier) {
    Text(text = label)
    Text(
      text = description,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    )
    Column(
      modifier = Modifier.padding(horizontal = 2.dp)
    ) {
      content()
    }
  }
}