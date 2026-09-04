package com.alveteg.simon.minutelauncher.settings.components

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ToggleInput(
  label: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  GenericRowInput(
    label = label,
    description = description,
    modifier = modifier
  ) {
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}