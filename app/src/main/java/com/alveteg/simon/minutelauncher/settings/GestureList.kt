package com.alveteg.simon.minutelauncher.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.home.dashboard.AppList
import com.alveteg.simon.minutelauncher.home.dashboard.SearchBar
import com.alveteg.simon.minutelauncher.utilities.Gesture
import timber.log.Timber

@Composable
fun GestureList(
  onNavigate: (UiEvent.Navigate) -> Unit,
  viewModel: SettingsViewModel = hiltViewModel(),
  gesture: Gesture
) {

  LaunchedEffect(key1 = true) {
    Timber.d("launched effect")
    viewModel.uiEvent.collect { event ->
      Timber.d("event: $event")
      when (event) {
        is UiEvent.Navigate -> onNavigate(event)
        else -> Unit
      }
    }
  }
  val apps by viewModel.filteredApps.collectAsState(initial = emptyList())
  val searchText by viewModel.searchTerm.collectAsState()

  var searchHeight by remember { mutableStateOf(0.dp) }
  val density = LocalDensity.current

  Surface(
    color = MaterialTheme.colorScheme.background
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      AppList(
        apps = apps,
        onAppClick = { viewModel.onEvent(SettingsEvent.SetAppGesture(it.app, gesture)) },
        searchHeight = searchHeight
      )
      SearchBar(
        searchText = searchText,
        onSearch = {
          apps.firstOrNull()?.let {
            viewModel.onEvent(SettingsEvent.SetAppGesture(it.app, gesture))
          }
        },
        topPadding = 0.dp,
        bottomPadding = 16.dp,
        onGloballyPositioned = {
          with(density) {
            searchHeight = it.toDp()
          }
        },
        onEvent = viewModel::onEvent
      )
    }
  }
}