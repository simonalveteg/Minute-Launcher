package com.alveteg.simon.minutelauncher.utilities

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.AppInfo

fun Modifier.clearFocusOnKeyboardDismiss(): Modifier = composed {
  var isFocused by remember { mutableStateOf(false) }
  var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }
  if (isFocused) {
    val imeIsVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
    val focusManager = LocalFocusManager.current
    LaunchedEffect(imeIsVisible) {
      if (imeIsVisible) {
        keyboardAppearedSinceLastFocused = true
      } else if (keyboardAppearedSinceLastFocused) {
        focusManager.clearFocus()
      }
    }
  }
  onFocusEvent {
    if (isFocused != it.isFocused) {
      isFocused = it.isFocused
      if (isFocused) {
        keyboardAppearedSinceLastFocused = false
      }
    }
  }
}


fun List<AppInfo>.filterBySearchTerm(searchTerm: String) : List<AppInfo> {
  return filter { appInfo ->
    appInfo.getTitle().lowercase().filterNot { it.isWhitespace() }
      .contains(searchTerm.lowercase().filterNot { it.isWhitespace() })
  }.sortedBy { it.getTitle().lowercase() }
}