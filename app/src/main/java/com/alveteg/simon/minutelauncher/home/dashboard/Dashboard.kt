package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.home.ScreenState
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
  screenState: ScreenState,
  onEvent: (Event) -> Unit,
  searchText: String,
  apps: List<AppInfo>,
  offsetY: Animatable<Float, AnimationVector1D>,
  onAppClick: (AppInfo) -> Unit,
  onSearch: KeyboardActionScope.() -> Unit
) {
  AnimatedVisibility(
    visible = screenState.isDashboard(),
    enter = fadeIn(),
    exit = fadeOut(tween(150))
  ) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val dashboardOffset = remember { Animatable(0f) }
    val hapticFeedback = LocalHapticFeedback.current

    val nestedScrollConnection = remember {
      object : NestedScrollConnection {
        val THRESHOLD = 50f
        var thresholdTriggered = false

        fun checkThreshold(offset: Float) {
          val isPastThreshold = abs(offset) > THRESHOLD
          if (isPastThreshold && !thresholdTriggered) {
            thresholdTriggered = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
          } else if (!isPastThreshold && thresholdTriggered) {
            thresholdTriggered = false
          }
        }

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
          if (source != NestedScrollSource.UserInput || dashboardOffset.value == 0f) return Offset.Zero

          val weight = (abs(dashboardOffset.value) - THRESHOLD) / THRESHOLD
          val easingFactor = (1 - weight * 0.85f) * 0.10f
          val easedDelta = available.y * easingFactor

          coroutineScope.launch {
            val next = dashboardOffset.value + easedDelta
            val newValue = if (abs(next) < 0.5f) 0f else next
            dashboardOffset.snapTo(newValue)
            checkThreshold(newValue)
          }
          return available
        }

        override fun onPostScroll(
          consumed: Offset,
          available: Offset,
          source: NestedScrollSource
        ): Offset {
          val overscrolling = available.y < 0 && !listState.canScrollBackward
          if (source != NestedScrollSource.UserInput || !overscrolling) {
            return super.onPostScroll(consumed, available, source)
          }

          val weight = (abs(dashboardOffset.value) - THRESHOLD) / THRESHOLD
          val easingFactor = (1 - weight * 0.85f) * 0.10f
          val easedDelta = available.y * easingFactor

          val newOffset = dashboardOffset.value + easedDelta

          coroutineScope.launch {
            dashboardOffset.snapTo(newOffset)
            checkThreshold(newOffset)
          }
          return Offset(0f, available.y)
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
          if (dashboardOffset.value == 0f) return Velocity.Zero

          val continuesGesture = sign(available.y) == sign(dashboardOffset.value)

          if (abs(dashboardOffset.value) > THRESHOLD && continuesGesture) {
            onEvent(HomeEvent.DismissDashboard)
            dashboardOffset.animateDecay(available.y.coerceIn(-2000f, -300f), exponentialDecay(3f))
            return Velocity.Zero
          }

          dashboardOffset.animateTo(0f, spring(0.55f, 800f))
          thresholdTriggered = false
          return available
        }
      }
    }

    val density = LocalDensity.current
    val peekHeight = remember { Animatable(0f) }
    val navbarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LaunchedEffect(navbarHeight) {
      peekHeight.animateTo(navbarHeight.value + 80f, spring(0.74f, 550f))
    }
    DisposableEffect(Unit) {
      onDispose {
        onEvent(HomeEvent.UpdateSearch(""))
      }
    }
    var searchHeight by remember { mutableStateOf(0.dp) }
    val scaffoldState = rememberBottomSheetScaffoldState()

    BackHandler(enabled = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
      coroutineScope.launch {
        scaffoldState.bottomSheetState.partialExpand()
      }
    }

    BottomSheetScaffold(
      scaffoldState = scaffoldState,
      sheetPeekHeight = (peekHeight.value + dashboardOffset.value).dp.coerceAtLeast(0.dp),
      sheetDragHandle = {},
      sheetContent = {
        DashboardBottomSheet(
          searchText = searchText,
          onSearch = onSearch,
          onEvent = onEvent,
          apps = apps,
          onGloballyPositioned = {
            searchHeight = with(density) { it.toDp() }
          },
          onSearchFocused = {
            coroutineScope.launch {
              scaffoldState.bottomSheetState.partialExpand()
            }
          }
        )
      },
      containerColor = Color.Transparent
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .nestedScroll(nestedScrollConnection)
      ) {
        AppList(
          apps = apps,
          offset = (offsetY.value + dashboardOffset.value).dp,
          onAppClick = onAppClick,
          listState = listState,
          searchHeight = searchHeight
        )
        if (scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                coroutineScope.launch {
                  scaffoldState.bottomSheetState.partialExpand()
                }
              }
          )
        }
      }
    }
  }
}