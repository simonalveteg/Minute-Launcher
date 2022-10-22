package com.example.android.minutelauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

      Column {
        Text(text = "Set apps to open when swiping left or right")
        Row {
          Button(onClick = {
            selectedDirection = GestureDirection.UPPER_RIGHT
            appSelectorVisible.value = true
          }) {
            Text(
              text = gestureApps.value[GestureDirection.UPPER_RIGHT]?.appTitle ?: "none"
            )
          }
          Button(onClick = {
            selectedDirection = GestureDirection.UPPER_LEFT
            appSelectorVisible.value = true
          }) {
            Text(
              text = gestureApps.value[GestureDirection.UPPER_LEFT]?.appTitle ?: "none"
            )
          }
        }
        Row {
          Button(onClick = {
            selectedDirection = GestureDirection.LOWER_RIGHT
            appSelectorVisible.value = true
          }) {
            Text(
              text = gestureApps.value[GestureDirection.LOWER_RIGHT]?.appTitle ?: "none"
            )
          }
          Button(onClick = {
            selectedDirection = GestureDirection.LOWER_LEFT
            appSelectorVisible.value = true
          }) {
            Text(
              text = gestureApps.value[GestureDirection.LOWER_LEFT]?.appTitle ?: "none"
            )
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