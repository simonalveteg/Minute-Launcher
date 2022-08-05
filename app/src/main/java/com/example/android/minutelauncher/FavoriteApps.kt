package com.example.android.minutelauncher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FavoriteApps(
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val totalUsage by viewModel.getTotalUsage()
  val favorites by viewModel.favoriteApps.collectAsState(initial = emptyList())
  Surface(Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Bottom,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(totalUsage.toTimeUsed())
      favorites.forEach { app ->
        val appUsage by viewModel.getUsageForApp(app)
        AppCard(
          app.appTitle,
          appUsage,
          { viewModel.onEvent(Event.ShowAppInfo(app)) }
        ) { viewModel.onEvent(Event.OpenApplication(app)) }
      }
      Spacer(modifier = Modifier.height(150.dp)) // TODO: Don't use hardcoded dp value
    }
  }
}