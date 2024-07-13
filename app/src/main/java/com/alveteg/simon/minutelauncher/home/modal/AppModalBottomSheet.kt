package com.alveteg.simon.minutelauncher.home.modal

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.MinuteAccessibilityService
import com.alveteg.simon.minutelauncher.data.AccessTimerMapping
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.isAccessibilityServiceEnabled
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
  appInfo: AppInfo?,
  timerMappings: List<AccessTimerMapping>,
  onDismiss: () -> Unit,
  onEvent: (Event) -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val visible = appInfo != null
  var timerVisible by remember { mutableStateOf(false) }

  if (visible) {
    val mContext = LocalContext.current
    appInfo!!
    val sheetState = rememberModalBottomSheetState()
    val timerSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
      onDismissRequest = onDismiss,
      sheetState = sheetState,
      dragHandle = {},
      windowInsets = WindowInsets(bottom = 0.dp)
    ) {
      Spacer(modifier = Modifier.height(4.dp))
      BackHandler(true) { onDismiss() }
      AppModal(
        appInfo = appInfo,
        timerMapping = timerMappings,
        onEvent = onEvent,
        onConfirmation = {
          onEvent(HomeEvent.LaunchActivity(appInfo))
          onDismiss()
        },
        onCancel = {
          val isAccessibilityServiceEnabled =
            isAccessibilityServiceEnabled(mContext, MinuteAccessibilityService::class.java)
          if (isAccessibilityServiceEnabled) {
            MinuteAccessibilityService.turnScreenOff()
            onDismiss()
          } else {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            ContextCompat.startActivity(mContext, intent, null)
          }
        },
        onChangeTimer = {
          timerVisible = true
          coroutineScope.launch {
            sheetState.hide()
            timerSheetState.expand()
          }
        }
      )
      Spacer(modifier = Modifier.height(4.dp))
    }
    if (timerVisible) {
      TimerBottomSheet(
        appInfo = appInfo,
        timerMappings = timerMappings,
        sheetState = timerSheetState,
        onDismissRequest = {
          timerVisible = false
          coroutineScope.launch {
            timerSheetState.hide()
            sheetState.show()
          }
        },
        onEvent = onEvent
      )
    }
  }
}