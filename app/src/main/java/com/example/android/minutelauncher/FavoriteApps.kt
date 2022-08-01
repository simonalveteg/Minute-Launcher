package com.example.android.minutelauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FavoriteApps(
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val totalUsage by viewModel.getTotalUsage()
    val favorites = viewModel.favoriteApps

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(totalUsage.toTimeUsed())
        }
        items(favorites) { app ->
            val appTitle by viewModel.getAppTitle(app)
            val appUsage by viewModel.getUsageForApp(app.activityInfo.packageName)
            AppCard(
                appTitle,
                appUsage,
                { viewModel.onEvent(Event.ShowAppInfo(app)) }
            ) { viewModel.onEvent(Event.OpenApplication(app)) }
        }
    }
}