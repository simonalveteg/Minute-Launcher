package com.alveteg.simon.minutelauncher.settings.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun LazyListScope.settingsSection(
  title: String,
  description: String? = null,
  modifier: Modifier = Modifier,
  content: LazyListScope.() -> Unit
) {
  item {
    Text(
      text = title,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.secondary,
      modifier = modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
    )
    if (description != null) {
      Text(
        text = description,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
      )
    }
    Spacer(Modifier.height(12.dp))
  }
  content()
  item {
    Spacer(Modifier.height(16.dp))
  }
}