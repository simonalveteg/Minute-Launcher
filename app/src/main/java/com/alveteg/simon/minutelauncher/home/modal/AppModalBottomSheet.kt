package com.alveteg.simon.minutelauncher.home.modal

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
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
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.MinuteDeviceAdminReceiver
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent
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
  var nameChangeVisible by remember { mutableStateOf(false) }

  if (visible) {
    val mContext = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val timerSheetState = rememberModalBottomSheetState()
    val nameChangeSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
      onDismissRequest = onDismiss,
      sheetState = sheetState,
      dragHandle = {},
    ) {
      Spacer(modifier = Modifier.height(4.dp))
      BackHandler(true) { onDismiss() }
      AppModal(
        appInfo = appInfo,
        onEvent = onEvent,
        onConfirmation = {
          onEvent(HomeEvent.LaunchActivity(appInfo))
          onDismiss()
        },
        onCancel = {
          val dpm = mContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
          val admin = ComponentName(mContext, MinuteDeviceAdminReceiver::class.java)

          if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
          } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
              putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
            }
            mContext.startActivity(intent)
          }
        },
        onChangeTimer = {
          timerVisible = true
          coroutineScope.launch {
            sheetState.hide()
            timerSheetState.expand()
          }
        },
        onEditName = {
          nameChangeVisible = true
          coroutineScope.launch {
            sheetState.hide()
            nameChangeSheetState.expand()
          }
        }
      )
      Spacer(modifier = Modifier.height(4.dp))
    }
    if (timerVisible) {
      TimerBottomSheet(
        appInfo = appInfo,
        sheetState = timerSheetState,
        onDismissRequest = {
          timerVisible = false
          coroutineScope.launch {
            sheetState.show()
          }
        },
        onEvent = onEvent
      )
    }
    if (nameChangeVisible) {
      NameBottomSheet(
        appInfo = appInfo,
        sheetState = nameChangeSheetState,
        onDismissRequest = {
          coroutineScope.launch {
            sheetState.show()
          }
          nameChangeVisible = false
        },
        onEvent = onEvent
      )
    }
  }
}