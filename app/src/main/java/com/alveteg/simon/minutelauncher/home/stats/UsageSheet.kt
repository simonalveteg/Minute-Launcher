package com.alveteg.simon.minutelauncher.home.stats

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.alveteg.simon.minutelauncher.data.sumOf
import com.alveteg.simon.minutelauncher.data.toTimeUsed
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

@Composable
fun UsageSheet(
  apps: List<AppInfo>,
) {

  var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

  val sortedStats: List<UsageStatistics> = remember(apps) {
    (6 downTo 0).map { daysAgo ->
      val date = LocalDate.now().minusDays(daysAgo.toLong())
      UsageStatistics(
        packageName = "",
        usageDate = date,
        usageDuration = apps.flatMap { it.usage }
          .filter { it.usageDate == date }
          .sumOf { it.usageDuration }
      )
    }
  }

  val appStatistics = remember(apps) {
    apps
      .map { app ->
        app.copy(
          usage = app.usage.filter { it.usageDate >= LocalDate.now().minusDays(7) }
        )
      }
      .filter { it.usage.sumOf { usage -> usage.usageDuration } > 1.seconds }
      .sortedByDescending { it.usage.sumOf { usage -> usage.usageDuration } }
  }

  val filteredAppStatistics = remember(appStatistics, selectedDate) {
    appStatistics
      .map { app ->
        app.copy(
          usage = app.usage.filter { it.usageDate == selectedDate }
            .takeIf { selectedDate != null } ?: app.usage
        )
      }
      .filter { it.usage.sumOf { usage -> usage.usageDuration } > 1.seconds }
      .sortedByDescending { it.usage.sumOf { usage -> usage.usageDuration } }
  }


  Surface(
    modifier = Modifier
      .heightIn(min = 220.dp, max = 600.dp)
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape = MaterialTheme.shapes.large,
  ) {
    Column {
      UsageBarGraph(
        usageStatistics = sortedStats,
        selectedDate = selectedDate,
        onDateSelectionChange = {
          selectedDate = it
        }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .animateContentSize()
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
        ) {
          Text(
            text = "MOST USED APPS",
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
        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          userScrollEnabled = false
        ) {
          items(
            items = filteredAppStatistics.take(5),
            key = { it.app.packageName }
          ) { appInfo ->
            Row(
              modifier = Modifier
                .animateItem(
                  fadeInSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                  ),
                  fadeOutSpec = spring(stiffness = Spring.StiffnessHigh),
                  placementSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                  )
                )
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
                  text = appInfo.usage.toTimeUsed(),
                  fontFamily = archivoFamily
                )
                Text(
                  text = " (${
                    (appStatistics.filter { it.app.packageName == appInfo.app.packageName }
                      .sumOf { it.usage.sumOf { it.usageDuration } } / 7).toTimeUsed()
                  })",
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
}