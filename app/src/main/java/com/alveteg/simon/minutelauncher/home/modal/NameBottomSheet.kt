package com.alveteg.simon.minutelauncher.home.modal

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameBottomSheet(
  sheetState: SheetState,
  appInfo: AppInfo,
  onDismissRequest: () -> Unit,
  onEvent: (Event) -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current
  var displayName by remember(appInfo) {
    mutableStateOf(appInfo.app.displayTitle ?: "")
  }

  val confirmAction = {
    keyboardController?.hide()
    focusManager.clearFocus()

    if (displayName.isBlank()) {
      onEvent(HomeEvent.ResetDisplayName(appInfo.app))
    } else {
      onEvent(HomeEvent.SetDisplayName(appInfo.app, displayName))
    }
    coroutineScope.launch {
      sheetState.hide()
    }.invokeOnCompletion {
      if (!sheetState.isVisible) {
        onDismissRequest()
      }
    }
  }

  MinuteBottomSheet(
    onDismissRequest = onDismissRequest, sheetState = sheetState,
    title = stringResource(R.string.title_set_display_name),
  ) {
    OutlinedTextField(
      value = displayName,
      onValueChange = { displayName = it },
      modifier = Modifier.fillMaxWidth(),
      label = { Text(appInfo.app.appTitle, fontFamily = archivoFamily) },
      singleLine = true,
      shape = MaterialTheme.shapes.medium,
      trailingIcon = {
        IconButton(
          enabled = displayName.isNotEmpty(),
          onClick = {
            displayName = ""
            onEvent(HomeEvent.ResetDisplayName(appInfo.app))
          }
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Undo,
            contentDescription = stringResource(R.string.label_reset_to_default),
          )
        }
      },
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(
        onDone = {
          defaultKeyboardAction(ImeAction.Done)
          confirmAction()
        })
    )
  }
}
