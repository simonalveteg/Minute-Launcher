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

@Composable
fun SettingsScreen(
  viewModel: LauncherViewModel = hiltViewModel(),
  onNavigate: (String) -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  val gestureApps = uiState.gestureApps.collectAsState(initial = emptyMap())
  val appSelectorVisible = remember { mutableStateOf(false) }
  var selectedDirection: GestureDirection? = null

  Surface {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Button(onClick = { onNavigate("main") }) {
        Text(text = "GO BACK")
      }
      LazyVerticalGrid(columns = GridCells.Fixed(2)){
        item {
          val gestureDirection = GestureDirection.UPPER_RIGHT
          val app = gestureApps.value[gestureDirection]
          GestureAppCard(
            app = app,
            onLongClick = {
              app?.let { viewModel.onEvent(Event.ClearAppGesture(gestureDirection)) }
            }
          ) {
            selectedDirection = gestureDirection
            appSelectorVisible.value = true
          }
        }
        item {
          val gestureDirection = GestureDirection.UPPER_LEFT
          val app = gestureApps.value[gestureDirection]
          GestureAppCard(
            app = app,
            onLongClick = {
              app?.let { viewModel.onEvent(Event.ClearAppGesture(gestureDirection)) }
            }
          ) {
            selectedDirection = gestureDirection
            appSelectorVisible.value = true
          }
        }
        item {
          val gestureDirection = GestureDirection.LOWER_RIGHT
          val app = gestureApps.value[gestureDirection]
          GestureAppCard(
            app = app,
            onLongClick = {
              app?.let { viewModel.onEvent(Event.ClearAppGesture(gestureDirection)) }
            }
          ) {
            selectedDirection = gestureDirection
            appSelectorVisible.value = true
          }
        }
        item {
          val gestureDirection = GestureDirection.LOWER_LEFT
          val app = gestureApps.value[gestureDirection]
          GestureAppCard(
            app = app,
            onLongClick = {
              app?.let { viewModel.onEvent(Event.ClearAppGesture(gestureDirection)) }
            }
            ) {
            selectedDirection = gestureDirection
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
            selectedDirection?.let {
              viewModel.onEvent(Event.SetAppGesture(app, it))
            }
            appSelectorVisible.value = false
          }
        ) {
          appSelectorVisible.value = false
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GestureAppCard(app: UserApp?, onLongClick: () -> Unit, onClick: () -> Unit) {
  Surface(
    tonalElevation = 4.dp,
    modifier = Modifier
      .padding(32.dp)
      .combinedClickable(onLongClick = {
        onLongClick()
      }) {
        onClick()
      }
  ) {
    Text(
      text = app?.appTitle ?: "none"
    )
  }
}