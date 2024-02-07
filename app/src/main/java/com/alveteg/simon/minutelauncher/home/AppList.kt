package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.utilities.thenIf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConstraintLayoutScope.AppList(
  apps: List<AppInfo>,
  offset: Dp,
  alpha: Float,
  constraintReference: ConstrainedLayoutReference,
  constraints: ConstrainScope.() -> Unit,
  onAppClick: (AppInfo) -> Unit,
  state: LazyListState = rememberLazyListState(),
  header: @Composable (() -> Unit )? = null
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
      items(items = apps, key = { it.app.id }) { appInfo ->
        val appTitle = appInfo.app.appTitle
        val appUsage = appInfo.usage
        Box(
          modifier = Modifier.animateItemPlacement(
            animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
          )
        ) {
          AppCard(appTitle, appUsage) { onAppClick(appInfo) }
        }
      }
    }
  }
}