package com.example.android.minutelauncher

import android.util.Log
import android.widget.Toast
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
                else -> {
                    Log.d("SCREEN", "back pressed")
                    bottomSheetScaffoldState.bottomSheetState.collapse()
                }
            }
        }
    }
    
    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            AppList()
        },
        sheetGesturesEnabled = true,
    ) {
        FavoriteApps()
    }
}
