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
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.home.rememberActionBarState
import com.alveteg.simon.minutelauncher.home.stats.UsageBarGraph
import com.alveteg.simon.minutelauncher.home.stats.UsageCard
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBottomSheet(
  scaffoldState: BottomSheetScaffoldState,
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

  val actionBarState = rememberActionBarState()

  var showUsageStatistics by remember { mutableStateOf(false) }
  val targetSheetValue = scaffoldState.bottomSheetState.targetValue
  val currentSheetValue = scaffoldState.bottomSheetState.currentValue
  LaunchedEffect(key1 = targetSheetValue, key2 = currentSheetValue) {
    when (targetSheetValue) {
      SheetValue.PartiallyExpanded -> {
        if (currentSheetValue == SheetValue.PartiallyExpanded) {
          showUsageStatistics = false
          actionBarState.close()
        }
      }

      else -> showUsageStatistics = true
    }
  }
  val sevenDayAverage = remember(usageStats) {
    usageStats.sumOf { it.usageDuration }.div(7)
  }
  val usageToday = remember(usageStats) {
    usageStats.filter { it.usageDate == LocalDate.now() }.sumOf { it.usageDuration }
  }
  val usage = remember(showUsageStatistics) {
    if (showUsageStatistics) usageStats else emptyList()
  }

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
        usage = sevenDayAverage,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
      Spacer(modifier = Modifier.width(16.dp))
      UsageCard(
        label = "Today",
        usage = usageToday,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
    }
    UsageBarGraph(usageStatistics = usage)
    DashboardActionBar(actionBarState = actionBarState, onEvent = onEvent)
  }
}