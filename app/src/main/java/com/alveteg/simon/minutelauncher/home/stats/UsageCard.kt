package com.alveteg.simon.minutelauncher.home.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed

@Composable
fun UsageCard(
  label: String,
  usage: Long,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.background,
    shape = MaterialTheme.shapes.large,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
          text = label.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      fontFamily = archivoFamily
      )
      Text(
        text = usage.toTimeUsed(),
        style = MaterialTheme.typography.headlineSmall,
        fontFamily = archivoBlackFamily
      )
    }
  }
}