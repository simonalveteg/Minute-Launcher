package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.stats.UsageSheet

@Composable
fun DashboardBottomSheet(
  searchText: String,
  apps: List<AppInfo>,
  onSearch: KeyboardActionScope.() -> Unit,
  onEvent: (Event) -> Unit,
  onGloballyPositioned: (Int) -> Unit = {},
  onSearchFocused: () -> Unit
) {
  val bottomPadding = 8
  val topPadding = 16
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .navigationBarsPadding()
      .padding(horizontal = 16.dp)
      .padding(bottom = bottomPadding.dp, top = topPadding.dp)
      .verticalScroll(scrollState)
  ) {
    SearchBar(
      searchText = searchText,
      onSearch = onSearch,
      onGloballyPositioned = onGloballyPositioned,
      onSearchFocused = onSearchFocused,
      onEvent = onEvent
    )
    Spacer(Modifier.height(8.dp))
    UsageSheet(apps = apps)
    DashboardActionBar(onEvent = onEvent)
  }
}