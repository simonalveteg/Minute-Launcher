package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.AppCard
import timber.log.Timber

@Composable
fun AppList(
  apps: List<AppInfo>,
  onAppClick: (AppInfo) -> Unit,
  offsetY: Animatable<Float, AnimationVector1D>? = null,
  searchHeight: Dp = 0.dp
) {
  val listState = rememberLazyListState()
  val offset = offsetY?.value?.dp ?: 0.dp
  val selectedApps = remember { mutableStateListOf<AppInfo>() }

  LazyColumn(
    state = listState,
    verticalArrangement = Arrangement.Bottom,
    reverseLayout = true,
    modifier = Modifier
      .fillMaxSize()
      .offset(y = offset)
  ) {
    item {
      Spacer(
        modifier = Modifier
          .navigationBarsPadding()
          .height(searchHeight + 8.dp)
      )
    }
    items(items = apps) { appInfo ->
      val appTitle = appInfo.app.appTitle
      val appUsage = appInfo.usage
      val isSelected by remember(appInfo, selectedApps) {
        derivedStateOf { selectedApps.contains(appInfo) }
      }
      AppCard(
        appTitle = appTitle,
        appUsage = appUsage,
        selected = isSelected,
        onLongClick = {
          selectedApps.addOrRemove(appInfo)
          Timber.d("LONG CLICK")
        }
      ) { onAppClick(appInfo) }
    }
    item {
      Spacer(modifier = Modifier.statusBarsPadding())
    }
  }
}

fun MutableList<AppInfo>.addOrRemove(app: AppInfo) {
  if (contains(app)) remove(app) else add(app)
}