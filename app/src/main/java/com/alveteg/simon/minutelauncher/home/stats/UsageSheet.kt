package com.alveteg.simon.minutelauncher.home.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.data.toTimeUsed
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import timber.log.Timber
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun UsageSheet(
  usageStatistics: List<UsageStatistics>,
  apps: List<AppInfo>,
) {
  val sortedStats = usageStatistics.sortedBy { it.usageDate }

  var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

  val appStatistics = apps
    .filter { app ->
      app.usage
        .filter {
          if (selectedDate != null) it.usageDate == selectedDate
          else it.usageDate >= LocalDate.now().minusDays(7)
        }
        .sumOf { it.usageDuration } > 0
    }
    .sortedByDescending { app -> app.usage.sumOf { it.usageDuration } }



  Surface(
    modifier = Modifier
      .heightIn(min = 220.dp, max = 600.dp)
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape = MaterialTheme.shapes.large,
  ) {
    Column {
      UsageBarGraph(usageStatistics = sortedStats) { index ->
        selectedDate = index?.let {
          LocalDate.now().minusDays(7 - it.toLong())
        }
      }
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
        ) {
          val date = selectedDate?.dayOfWeek?.getDisplayName(TextStyle.FULL, Locale.getDefault())?.uppercase() ?: "WEEK"
          Text(
            text = "MOST USED APPS ($date)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = archivoFamily,
            modifier = Modifier.weight(1f)
          )
          Text(
            text = "DURATION (AVG)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = archivoFamily,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
          )
        }
        HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
        appStatistics.take(5).forEach { appInfo ->
          val usageDuration = appInfo.usage
            .filter {
              if (selectedDate != null) it.usageDate == selectedDate
              else it.usageDate >= LocalDate.now().minusDays(7)
            }
            .sumOf { it.usageDuration }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp)
              .height(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = appInfo.app.displayTitle ?: appInfo.app.appTitle,
              fontFamily = archivoFamily
            )
            Row {
              Text(
                text = usageDuration.toTimeUsed(),
                fontFamily = archivoFamily
              )
              Text(
                text = " (${(usageDuration / 7).toTimeUsed()})",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = archivoFamily
              )
            }
          }
        }
      }
    }
  }
}