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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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
    appInfo.app.appTitle.lowercase().filterNot { it.isWhitespace() }
      .contains(searchTerm.lowercase().filterNot { it.isWhitespace() })
  }.sortedBy { (it.app.displayTitle ?: it.app.appTitle).lowercase() }
}

@Suppress("UNCHECKED_CAST")
fun <T1, T2, T3, T4, T5, T6, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<Any?> ->
  transform(
    args[0] as T1,
    args[1] as T2,
    args[2] as T3,
    args[3] as T4,
    args[4] as T5,
    args[5] as T6
  )
}
