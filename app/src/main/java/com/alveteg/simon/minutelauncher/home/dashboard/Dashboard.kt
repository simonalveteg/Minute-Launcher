package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.AppCard
import com.alveteg.simon.minutelauncher.home.ScreenState
import com.alveteg.simon.minutelauncher.utilities.Gesture
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
  screenState: ScreenState,
  gestureApps: Map<Gesture, App>,
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
      peekHeight.animateTo(100f, tween(200))
    }
    DisposableEffect(Unit) {
      onDispose {
        onEvent(Event.UpdateSearch(""))
      }
    }
    var searchHeight by remember { mutableStateOf(0.dp) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val density = LocalDensity.current
    BottomSheetScaffold(
      scaffoldState = scaffoldState,
      sheetPeekHeight = peekHeight.value.dp,
      sheetDragHandle = {},
      sheetContent = {
        DashboardBottomSheet(
          searchText = searchText,
          onSearch = onSearch,
          onEvent = onEvent,
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
      val listState = rememberLazyListState()
      LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.Bottom,
        reverseLayout = true,
        modifier = Modifier
          .fillMaxSize()
          .offset(y = offsetY.value.dp)
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
  }
}
