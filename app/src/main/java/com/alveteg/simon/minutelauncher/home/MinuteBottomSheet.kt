package com.alveteg.simon.minutelauncher.home

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.MinuteAccessibilityService
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.modal.AppModal
import com.alveteg.simon.minutelauncher.isAccessibilityServiceEnabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinuteBottomSheet(
  appInfo: AppInfo?,
  sheetState: SheetState,
  onDismiss: () -> Unit,
  onEvent: (Event) -> Unit
) {
  val visible = appInfo != null

  if (visible) {
    val mContext = LocalContext.current
    appInfo!!
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
        }
      )
      Spacer(modifier = Modifier.height(4.dp))
    }
  }
}