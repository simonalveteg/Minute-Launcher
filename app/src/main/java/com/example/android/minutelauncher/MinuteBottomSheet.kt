package com.example.android.minutelauncher

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
import com.example.android.minutelauncher.db.App

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinuteBottomSheet(
  app: App?,
  sheetState: SheetState,
  onDismiss: () -> Unit,
  onEvent: (Event) -> Unit,

) {
  val visible = app != null

  if (visible) {
    val mContext = LocalContext.current
    val app = app!!
    ModalBottomSheet(
      onDismissRequest = onDismiss,
      sheetState = sheetState,
      dragHandle = {},
      windowInsets = WindowInsets(bottom = 0.dp)
    ) {
      Spacer(modifier = Modifier.height(4.dp))
      BackHandler(true) { onDismiss() }
      AppModal(
        app = app,
        onEvent = onEvent,
        onConfirmation = {
          onEvent(Event.LaunchActivity(app))
          onEvent(Event.ClearModal)
        },
        onDismiss = {
          val isAccessibilityServiceEnabled =
            isAccessibilityServiceEnabled(mContext, MinuteAccessibilityService::class.java)
          if (isAccessibilityServiceEnabled) {
            MinuteAccessibilityService.turnScreenOff()
            onEvent(Event.ClearModal)
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