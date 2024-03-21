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
import com.alveteg.simon.minutelauncher.home.stats.UsageBarGraph
import com.alveteg.simon.minutelauncher.home.stats.UsageCard

@Composable
fun DashboardBottomSheet(
  searchText: String,
  onSearch: KeyboardActionScope.() -> Unit,
  onEvent: (Event) -> Unit,
  onGloballyPositioned: (Int) -> Unit = {},
  onSearchFocused: () -> Unit
) {
  val bottomPadding = 8
  val topPadding = 16

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
        usage = 10000000L,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
      Spacer(modifier = Modifier.width(16.dp))
      UsageCard(
        label = "Today",
        usage = 20000000L,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
    }
    UsageBarGraph()
    DashboardActionBar(onEvent = onEvent)
  }
}