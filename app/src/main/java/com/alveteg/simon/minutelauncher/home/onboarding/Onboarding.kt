package com.alveteg.simon.minutelauncher.home.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Onboarding(
  apps: List<AppInfo>,
  onEvent: (Event) -> Unit,
) {
  val coroutineScope = rememberCoroutineScope()
  val pagerState = rememberPagerState(pageCount = { 2 })

  BackHandler(enabled = pagerState.currentPage > 0) {
    coroutineScope.launch {
      pagerState.animateScrollToPage(pagerState.currentPage - 1)
    }
  }

  HorizontalPager(
    state = pagerState,
  ) { page ->
    when (page) {
      0 -> PermissionsScreen(
        onFinish = {
          coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
          }
        }
      )

      1 -> FavoriteSelectionScreen(
        apps = apps,
        onNext = { onEvent(HomeEvent.HideOnboarding) },
        onEvent = onEvent
      )
    }
  }
}

