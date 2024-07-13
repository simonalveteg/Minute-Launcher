package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.utilities.clearFocusOnKeyboardDismiss

@Composable
fun SearchBar(
  searchText: String,
  onSearch: KeyboardActionScope.() -> Unit,
  topPadding: Dp,
  bottomPadding: Dp,
  onGloballyPositioned: (Int) -> Unit = {},
  onSearchFocused: () -> Unit = {},
  onEvent: (Event) -> Unit
) {
  val focusRequester = remember { FocusRequester() }

  Surface(shape = MaterialTheme.shapes.large,
    tonalElevation = 8.dp,
    modifier = Modifier
      .navigationBarsPadding()
      .onGloballyPositioned {
        onGloballyPositioned(
          it.size.height
            .plus(bottomPadding.value)
            .plus(topPadding.value)
            .toInt()
        )
      }) {
    TextField(value = searchText,
      onValueChange = { onEvent(Event.UpdateSearch(it)) },
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(focusRequester)
        .onFocusChanged {
          if (it.hasFocus) {
            onSearchFocused()
          }
        }
        .clearFocusOnKeyboardDismiss(),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(onSearch = {
        onSearch()
        focusRequester.freeFocus()
      }),
      colors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent
      ),
      placeholder = {
        Text(
          text = "Search apps", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
      },
      textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search Icon",
          tint = LocalContentColor.current
        )
      },
      trailingIcon = {
        IconButton(onClick = {
          onEvent(Event.UpdateSearch(""))
        }
        ) {
          val tint = if (searchText.isNotBlank()) LocalContentColor.current else Color.Transparent
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Clear searchbar",
            tint = tint
          )
        }
      }
    )
  }
}