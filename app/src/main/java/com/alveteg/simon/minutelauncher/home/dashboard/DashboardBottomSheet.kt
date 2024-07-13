package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.home.stats.UsageBarGraph
import com.alveteg.simon.minutelauncher.home.stats.UsageCard
import java.time.LocalDate

@Composable
fun DashboardBottomSheet(
  searchText: String,
  onSearch: KeyboardActionScope.() -> Unit,
  onEvent: (Event) -> Unit,
  onGloballyPositioned: (Int) -> Unit = {},
  usageStatistics: List<UsageStatistics>,
  onSearchFocused: () -> Unit
) {
  val bottomPadding = 8
  val topPadding = 16
  val usageStats = usageStatistics.groupBy { it.usageDate }.mapValues { entry ->
    entry.value.sumOf { it.usageDuration }
  }.map { UsageStatistics("", it.key, it.value) }

  Column(
    modifier = Modifier
      .navigationBarsPadding()
      .padding(horizontal = 16.dp)
      .padding(bottom = bottomPadding.dp, top = topPadding.dp)
  ) {
    SearchBar(
      searchText = searchText,
      onSearch = onSearch,
      topPadding = topPadding.dp,
      bottomPadding = bottomPadding.dp,
      onGloballyPositioned = onGloballyPositioned,
      onSearchFocused = onSearchFocused,
      onEvent = onEvent
    )
    Spacer(Modifier.height(8.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
    ) {
      UsageCard(
        label = "7 day average",
        usage = usageStats.sumOf { it.usageDuration }.div(7),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
      Spacer(modifier = Modifier.width(16.dp))
      UsageCard(
        label = "Today",
        usage = usageStats.filter { it.usageDate == LocalDate.now() }.sumOf { it.usageDuration },
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
    }
    UsageBarGraph(usageStatistics = usageStats)
    DashboardActionBar(onEvent = onEvent)
  }
}