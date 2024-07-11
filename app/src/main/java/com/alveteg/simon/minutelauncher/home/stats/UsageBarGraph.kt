package com.alveteg.simon.minutelauncher.home.stats

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed

@Composable
fun UsageBarGraph(
  usageStatistics: List<UsageStatistics>
) {
  Surface(
    modifier = Modifier
      .height(220.dp)
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    color = MaterialTheme.colorScheme.background,
    shape = MaterialTheme.shapes.large,
    tonalElevation = 8.dp
  ) {
    LazyColumn {
      items(usageStatistics) {
        Text(text = "${it.usageDate}: ${it.usageDuration.toTimeUsed()}")
      }
    }
  }
}