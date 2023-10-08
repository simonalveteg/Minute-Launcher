package com.example.android.minutelauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.minutelauncher.db.App
import timber.log.Timber

@Composable
fun SettingsScreen(
  viewModel: LauncherViewModel = hiltViewModel(),
  onNavigate: (String) -> Unit
) {
  val gestureApps by viewModel.gestureApps.collectAsState(initial = emptyMap())
  val appSelectorVisible = remember { mutableStateOf(false) }
  val selectedDirection = remember {
    mutableStateOf<GestureDirection?>(null)
  }

  Surface {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Button(onClick = { onNavigate("main") }) {
        Text(text = "GO BACK")
      }
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center
      ) {
        item {
          val gestureDirection = GestureDirection.UPPER_RIGHT
          val app = gestureApps[gestureDirection]
          GestureAppCard(
            app = app,
            onLongClick = {
              app?.let { viewModel.onEvent(Event.ClearAppGesture(gestureDirection)) }
            }
          ) {
            selectedDirection.value = gestureDirection
            appSelectorVisible.value = true
          }
        }
        item {
          val gestureDirection = GestureDirection.UPPER_LEFT
          val app = gestureApps[gestureDirection]
          GestureAppCard(
            app = app,
            onLongClick = {
              app?.let { viewModel.onEvent(Event.ClearAppGesture(gestureDirection)) }
            }
          ) {
            selectedDirection.value = gestureDirection
            appSelectorVisible.value = true
          }
        }
        item {
          val gestureDirection = GestureDirection.LOWER_RIGHT
          val app = gestureApps[gestureDirection]
          GestureAppCard(
            app = app,
            onLongClick = {
              app?.let { viewModel.onEvent(Event.ClearAppGesture(gestureDirection)) }
            }
          ) {
            selectedDirection.value = gestureDirection
            appSelectorVisible.value = true
          }
        }
        item {
          val gestureDirection = GestureDirection.LOWER_LEFT
          val app = gestureApps[gestureDirection]
          GestureAppCard(
            app = app,
            onLongClick = {
              app?.let { viewModel.onEvent(Event.ClearAppGesture(gestureDirection)) }
            }
          ) {
            selectedDirection.value = gestureDirection
            appSelectorVisible.value = true
          }
        }
      }
    }
    AnimatedVisibility(
      visible = appSelectorVisible.value,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Surface {
        AppList(
          onAppPress = { app ->
            selectedDirection.value?.let {
              Timber.d("Selected ${app.appTitle} in direction $it")
              viewModel.onEvent(Event.SetAppGesture(app, it))
            }
            viewModel.onEvent(Event.UpdateSearch(""))
            appSelectorVisible.value = false
          }
        ) {
          viewModel.onEvent(Event.UpdateSearch(""))
          appSelectorVisible.value = false
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GestureAppCard(app: App?, onLongClick: () -> Unit, onClick: () -> Unit) {
  Surface(
    tonalElevation = 4.dp,
    modifier = Modifier
      .fillMaxSize()
      .height(120.dp)
      .combinedClickable(onLongClick = {
        onLongClick()
      }) {
        onClick()
      }
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
    ) {
      Text(
        text = app?.appTitle ?: "none",
        modifier = Modifier.align(Alignment.Center)
      )
    }
  }
}