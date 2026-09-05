package com.alveteg.simon.minutelauncher.home.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily

@Composable
fun PermissionPrompt(
  title: String,
  modifier: Modifier = Modifier,
  description: String? = null,
  enabled: Boolean = true,
  onClick: () -> Unit
) {

  val textDecoration = if (enabled) null else TextDecoration.LineThrough
  val icon = if (enabled) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Done
  val textColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
  val fontStyle = if (enabled) FontStyle.Normal else FontStyle.Italic

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .clickable(
        enabled = true, onClick = onClick
      ),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier.padding(start = 16.dp)
    ) {
      Text(
        text = title,
        color = textColor,
        fontStyle = fontStyle,
        fontFamily = archivoBlackFamily,
        style = MaterialTheme.typography.bodyLargeEmphasized,
        textDecoration = textDecoration
      )
      if (description != null) {
        Text(
          text = description,
          color = textColor,
          fontStyle = fontStyle,
          style = MaterialTheme.typography.bodySmall,
          textDecoration = textDecoration
        )
      }
    }
    FilledTonalIconButton(
      onClick = onClick,
      enabled = enabled,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = ""
      )
    }
  }
}