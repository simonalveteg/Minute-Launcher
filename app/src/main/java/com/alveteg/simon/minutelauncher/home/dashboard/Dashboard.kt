package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.home.ScreenState
import kotlinx.coroutines.launch

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
    exit = fadeOut()
  ) {
    val coroutineScope = rememberCoroutineScope()
    val peekHeight = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
      peekHeight.animateTo(100f, spring(0.74f, 550f))
    }
    DisposableEffect(Unit) {
      onDispose {
        onEvent(HomeEvent.UpdateSearch(""))
      }
    }
    var searchHeight by remember { mutableStateOf(0.dp) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val density = LocalDensity.current

    BackHandler(enabled = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
      coroutineScope.launch {
        scaffoldState.bottomSheetState.partialExpand()
      }
    }

    BottomSheetScaffold(
      scaffoldState = scaffoldState,
      sheetPeekHeight = peekHeight.value.dp,
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
      Box(modifier = Modifier.fillMaxSize()) {
        AppList(
          apps = apps,
          offsetY = offsetY,
          onAppClick = onAppClick,
          onEvent = onEvent,
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