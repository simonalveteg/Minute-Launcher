package com.alveteg.simon.minutelauncher.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.utilities.thenIf

@Composable
fun ConstraintLayoutScope.AppList(
  apps: List<AppInfo>,
  offset: Dp,
  alpha: Float,
  constraintReference: ConstrainedLayoutReference,
  constraints: ConstrainScope.() -> Unit,
  onAppClick: (AppInfo) -> Unit,
  state: LazyListState = rememberLazyListState(),
  header: @Composable (() -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .constrainAs(constraintReference) { constraints() }
      .graphicsLayer {
        this.alpha = alpha
      }
      .offset(y = offset)
      .thenIf(header != null) { statusBarsPadding() },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 12.dp)
    ) {
      header?.invoke()
    }
    LazyColumn(
      state = state,
      verticalArrangement = Arrangement.Bottom,
      reverseLayout = true,
      modifier = Modifier.fillMaxSize()
    ) {
      items(items = apps) { appInfo ->
        val appTitle = appInfo.app.appTitle
        val appUsage = appInfo.usage
        AppCard(appTitle, appUsage) { onAppClick(appInfo) }
      }
    }
  }
}