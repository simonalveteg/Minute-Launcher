package com.example.android.minutelauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.lang.reflect.Method


@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
  onNavigate: (String) -> Unit,
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val mContext = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusRequester = remember { FocusRequester() }
  val coroutineScope = rememberCoroutineScope()
  val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
    bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed) {
      Log.d("MAIN_SCREEN", it.name)
      // When sheet is dragged into a collapsed state the keyboard should be hidden
      if (it.name != BottomSheetValue.Expanded.name) {
        focusRequester.freeFocus()
        keyboardController?.hide()
        viewModel.onEvent(Event.UpdateSearch(""))
      }
      true
    }
  )

  var currentAppModal by remember { mutableStateOf<UserApp?>(null) }

  val dialogSheetScaffoldState =
    rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden) {
      if (it == ModalBottomSheetValue.Hidden) currentAppModal = null
      true
    }
  LaunchedEffect(key1 = true) {
    Log.d("MAIN_SCREEN", "launched effect")
    viewModel.uiEvent.collect { event ->
      Log.d("MAIN_SCREEN", "event: $event")
      when (event) {
        is UiEvent.ShowToast -> Toast.makeText(mContext, event.text, Toast.LENGTH_SHORT).show()
        is UiEvent.OpenApplication -> {
          currentAppModal = event.app
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        is UiEvent.LaunchActivity -> mContext.startActivity(event.intent)
        is UiEvent.OpenAppDrawer -> {
          launch { bottomSheetScaffoldState.bottomSheetState.expand() }
          focusRequester.requestFocus()
        }
        is UiEvent.ExpandNotifications -> {
          setExpandNotificationDrawer(mContext,true)
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
      }
    }
  }

  LaunchedEffect(key1 = currentAppModal) {
    if (currentAppModal != null) {
      launch { dialogSheetScaffoldState.show() }
      keyboardController?.hide()
      focusRequester.freeFocus()
    } else {
      launch { dialogSheetScaffoldState.hide() }
    }
  }

  ModalBottomSheetLayout(
    sheetState = dialogSheetScaffoldState,
    sheetBackgroundColor = MaterialTheme.colorScheme.background,
    sheetContent = {
      Spacer(modifier = Modifier.height(4.dp))
      if (currentAppModal != null) {
        val app = currentAppModal!!
        AppModal(
          app = app,
          onEvent = viewModel::onEvent,
          onConfirmation = {
            viewModel.onEvent(Event.LaunchActivity(app))
            currentAppModal = null
          },
          onDismiss = {
            val isAccessibilityServiceEnabled =
              isAccessibilityServiceEnabled(mContext, MinuteAccessibilityService::class.java)
            if (isAccessibilityServiceEnabled) {
              MinuteAccessibilityService.turnScreenOff()
              currentAppModal = null
            } else {
              val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
              ContextCompat.startActivity(mContext, intent, null)
            }
          }
        )
      }
    }
  ) {
    BottomSheetScaffold(
      scaffoldState = bottomSheetScaffoldState,
      sheetPeekHeight = 0.dp,
      backgroundColor = Color.Transparent,
      sheetContent = {
        AppList(
          focusRequester = focusRequester,
          onAppPress = { viewModel.onEvent(Event.OpenApplication(it)) },
          onBackPressed = {
            coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() }
            viewModel.onEvent(Event.UpdateSearch(""))
          }
        )
      },
    ) {
      FavoriteApps(
        onNavigate = onNavigate
      )
    }
  }
}

@SuppressLint("WrongConstant")
fun setExpandNotificationDrawer(context: Context, expand: Boolean) {
  try {
    val statusBarService = context.getSystemService("statusbar")
    val methodName = if (expand) "expandNotificationsPanel" else "collapsePanels"
    val statusBarManager: Class<*> = Class.forName("android.app.StatusBarManager")
    val method: Method = statusBarManager.getMethod(methodName)
    method.invoke(statusBarService)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}
