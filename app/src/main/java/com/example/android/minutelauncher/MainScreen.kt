package com.example.android.minutelauncher

import android.util.Log
import android.widget.Toast
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
  onNavigate: (String) -> Unit,
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val mContext = LocalContext.current
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
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
  var openDialogApp by remember { mutableStateOf<UserApp?>(null) }

  LaunchedEffect(key1 = true) {
    Log.d("MAIN_SCREEN", "launched effect")
    viewModel.uiEvent.collect { event ->
      Log.d("MAIN_SCREEN", "event: $event")
      when (event) {
        is UiEvent.ShowToast -> Toast.makeText(mContext, event.text, Toast.LENGTH_SHORT).show()
        is UiEvent.StartActivity -> mContext.startActivity(event.intent)
        is UiEvent.OpenAppDrawer -> {
          launch { bottomSheetScaffoldState.bottomSheetState.expand() }
          focusRequester.requestFocus()
        }
      }
    }
  }

  BottomSheetScaffold(
    scaffoldState = bottomSheetScaffoldState,
    sheetPeekHeight = 0.dp,
    backgroundColor = Color.Transparent,
    sheetContent = {
      if (openDialogApp != null) {
        AppInfo(
          onFavorite = { viewModel.onEvent(Event.ToggleFavorite(openDialogApp!!)) },
          onHide = { viewModel.onEvent(Event.ToggleFavorite(openDialogApp!!)) },
          onUninstall = { viewModel.onEvent(Event.ToggleFavorite(openDialogApp!!)) },
          onDismiss = { openDialogApp = null }
        )
      }
      AppList(
        focusRequester = focusRequester,
        onLongPress = { openDialogApp = it },
        onBackPressed = {
          coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() }
          viewModel.onEvent(Event.UpdateSearch(""))
        }
      )
    },
  ) {
    FavoriteApps(
      onAppPressed = { openDialogApp = it },
      onNavigate = onNavigate
    )
  }
}
