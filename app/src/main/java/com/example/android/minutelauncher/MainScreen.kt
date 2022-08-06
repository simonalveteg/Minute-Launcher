package com.example.android.minutelauncher

import android.util.Log
import android.widget.Toast
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
  onNavigate: (UiEvent.Navigate) -> Unit,
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val mContext = LocalContext.current
  val bottomSheetExpanded = remember { mutableStateOf(false) }
  val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
    bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed) {
      Log.d("MAIN_SCREEN", it.name)
      if (it.name == BottomSheetValue.Expanded.name && !bottomSheetExpanded.value) {
        viewModel.onEvent(Event.SearchClicked)
      } else viewModel.onEvent(Event.DismissSearch)
      bottomSheetExpanded.value = it.name == BottomSheetValue.Expanded.name
      true
    }
  )
  var openDialogApp by remember { mutableStateOf<UserApp?>(null) }

  LaunchedEffect(key1 = true) {
    viewModel.uiEvent.collect { event ->
      Log.d("MAIN_SCREEN", "event: $event")
      when (event) {
        is UiEvent.ShowToast -> {
          Toast.makeText(mContext, event.text, Toast.LENGTH_SHORT).show()
        }
        is UiEvent.StartActivity -> {
          mContext.startActivity(event.intent)
        }
        is UiEvent.Navigate -> {
          onNavigate(event)
        }
        is UiEvent.ShowNotifications -> Unit
        is UiEvent.HideAppsList -> {
          launch { bottomSheetScaffoldState.bottomSheetState.collapse() }
        }
        is UiEvent.ShowAppsList -> {
          launch { bottomSheetScaffoldState.bottomSheetState.expand() }
        }
        is UiEvent.ShowAppInfo -> {
          openDialogApp = event.app
        }
        is UiEvent.DismissDialog -> {
          openDialogApp = null
        }
        else -> Unit
      }
    }
  }

  BottomSheetScaffold(
    scaffoldState = bottomSheetScaffoldState,
    sheetPeekHeight = 0.dp,
    sheetContent = {
      if (openDialogApp != null) {
        AppInfo(
          onFavorite = { viewModel.onEvent(Event.ToggleFavorite(openDialogApp!!)) },
          onHide = { viewModel.onEvent(Event.ToggleFavorite(openDialogApp!!)) },
          onUninstall = { viewModel.onEvent(Event.ToggleFavorite(openDialogApp!!)) },
          onDismiss = { viewModel.onEvent(Event.DismissDialog) }
        )
      }
      AppList()
    },
    sheetGesturesEnabled = true,
  ) {
    FavoriteApps()

  }
}
