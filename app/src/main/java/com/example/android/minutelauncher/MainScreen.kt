package com.example.android.minutelauncher

import android.content.pm.ResolveInfo
import android.util.Log
import android.widget.Toast
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val mContext = LocalContext.current
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )
    var openDialogApp by remember { mutableStateOf<ResolveInfo?>(null) }

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
                is UiEvent.HideAppsList -> {
                    Log.d("SCREEN", "back pressed")
                    bottomSheetScaffoldState.bottomSheetState.collapse()
                }
                is UiEvent.ShowAppInfo -> {
                    openDialogApp = event.app
                }
                is UiEvent.DismissDialog -> {
                    openDialogApp = null
                }
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
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
