package com.alveteg.simon.minutelauncher.home.modal

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.MinuteAccessibilityService
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.MinuteBottomSheet
import com.alveteg.simon.minutelauncher.home.SegmentedControl
import com.alveteg.simon.minutelauncher.isAccessibilityServiceEnabled
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
  appInfo: AppInfo?,
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
        onEvent = onEvent,
        onConfirmation = {
          onEvent(Event.LaunchActivity(appInfo))
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
      MinuteBottomSheet(
        onDismissRequest = {
          timerVisible = false
          coroutineScope.launch {
            timerSheetState.hide()
            sheetState.show()
          }
        },
        sheetState = timerSheetState
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Change app timer",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = archivoBlackFamily,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
          )
          Text(
            text = "The app timer decides how long you need to wait before being able to open the app. ",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontFamily = archivoFamily,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
          )
          SegmentedControl(
            items = setOf(0, 2, 5, 10, 15),
            selectedItem = appInfo.app.timer,
            onItemSelection = {
              onEvent(
                Event.UpdateApp(
                  appInfo.app.copy(timer = it)
                )
              )
            }
          )
          Spacer(modifier = Modifier.height(12.dp))
        }
      }
    }
  }
}