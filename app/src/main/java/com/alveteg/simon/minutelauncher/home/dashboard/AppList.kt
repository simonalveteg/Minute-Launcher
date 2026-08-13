package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.AppCard
import com.alveteg.simon.minutelauncher.home.HomeEvent
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun AppList(
  apps: List<AppInfo>,
  onAppClick: (AppInfo) -> Unit,
  onEvent: (Event) -> Unit,
  offsetY: Animatable<Float, AnimationVector1D>? = null,
  searchHeight: Dp = 0.dp
) {
  val listState = rememberLazyListState()
  val offset = offsetY?.value?.dp ?: 0.dp
  val coroutineScope = rememberCoroutineScope()

  val nestedScrollConnection = remember(offsetY) {
    object : NestedScrollConnection {

      override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
      ): Offset {
        val animatable = offsetY ?: return Offset.Zero
        if (source != NestedScrollSource.UserInput || animatable.value == 0f) return Offset.Zero

        val threshold = 100f
        val weight = (abs(animatable.value) - threshold) / threshold
        val easingFactor = (1 - weight * 0.85f) * 0.10f
        val easedDelta = available.y * easingFactor

        coroutineScope.launch {
          val next = animatable.value + easedDelta
          animatable.snapTo(if (abs(next) < 0.5f) 0f else next)
        }

        return available
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
      ): Offset {
        val animatable = offsetY ?: return super.onPostScroll(consumed, available, source)
        val overscrolling = available.y < 0 && !listState.canScrollBackward
        if (source != NestedScrollSource.UserInput || !overscrolling) {
          return super.onPostScroll(consumed, available, source)
        }

        val threshold = 100f
        val weight = (abs(animatable.value) - threshold) / threshold
        val easingFactor = (1 - weight * 0.85f) * 0.10f
        val easedDelta = available.y * easingFactor

        coroutineScope.launch {
          animatable.snapTo(animatable.value + easedDelta)
        }

        return Offset(0f, available.y)
      }

      override suspend fun onPreFling(available: Velocity): Velocity {
        val animatable = offsetY ?: return Velocity.Zero
        if (animatable.value == 0f) return Velocity.Zero

        if (abs(animatable.value) > 100f) {
          onEvent(HomeEvent.DismissDashboard)
        }
        animatable.animateTo(0f, spring(0.55f, 800f))
        return available
      }
    }
  }

  LazyColumn(
    state = listState,
    verticalArrangement = Arrangement.Bottom,
    reverseLayout = true,
    modifier = Modifier
      .fillMaxSize()
      .offset(y = offset)
      .nestedScroll(nestedScrollConnection)
  ) {
    item {
      Spacer(
        modifier = Modifier
          .navigationBarsPadding()
          .height(searchHeight + 16.dp)
      )
    }
    items(items = apps) { appInfo ->
      AppCard(appInfo) { onAppClick(appInfo) }
    }
    item {
      Spacer(modifier = Modifier.statusBarsPadding())
    }
  }
}
