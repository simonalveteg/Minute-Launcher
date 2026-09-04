package com.alveteg.simon.minutelauncher.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GenericColumnInput(
  label: String? = null,
  description: String? = null,
  modifier: Modifier = Modifier,
  content: (@Composable () -> Unit)? = null
) {
  Column(modifier) {
    InputLabels(label, description)
    Column(Modifier.padding(horizontal = 2.dp)) {
      if (content != null) content() else Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
fun GenericRowInput(
  label: String? = null,
  description: String? = null,
  modifier: Modifier = Modifier,
  content: (@Composable () -> Unit)? = null
) {
  Row(modifier, verticalAlignment = Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) {
      InputLabels(label, description)
    }
    if (content != null) content()
  }
}

@Composable
private fun InputLabels(label: String?, description: String?) {
  if (label != null) Text(text = label)
  if (description != null) {
    Text(
      text = description,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    )
  }
}