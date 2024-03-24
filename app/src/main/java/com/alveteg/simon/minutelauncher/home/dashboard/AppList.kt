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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.AppCard

@Composable
fun AppList(
  apps: List<AppInfo>,
  onAppClick: (AppInfo) -> Unit,
  offsetY: Animatable<Float, AnimationVector1D>? = null,
  searchHeight: Dp = 0.dp
  ) {
  val listState = rememberLazyListState()
  val offset = offsetY?.value?.dp ?: 0.dp

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
      AppCard(appTitle, appUsage) { onAppClick(appInfo) }
    }
    item {
      Spacer(modifier = Modifier.statusBarsPadding())
    }
  }
}