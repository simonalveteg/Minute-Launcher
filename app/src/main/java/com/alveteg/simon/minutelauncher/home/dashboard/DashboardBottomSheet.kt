package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.home.stats.UsageBarGraph
import com.alveteg.simon.minutelauncher.home.stats.UsageCard
import com.alveteg.simon.minutelauncher.utilities.clearFocusOnKeyboardDismiss

@Composable
fun DashboardBottomSheet(
  searchText: String,
  onSearch: KeyboardActionScope.() -> Unit,
  onEvent: (Event) -> Unit,
  onGloballyPositioned: (Int) -> Unit = {},
  onSearchFocused: () -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  val bottomPadding = 8
  val topPadding = 16

  Column(
    modifier = Modifier
      .navigationBarsPadding()
      .padding(horizontal = 16.dp)
      .padding(bottom = bottomPadding.dp, top = topPadding.dp)
  ) {
    Surface(
      shape = MaterialTheme.shapes.large,
      tonalElevation = 8.dp,
      modifier = Modifier
        .navigationBarsPadding()
        .onGloballyPositioned {
          onGloballyPositioned(it.size.height + bottomPadding + topPadding)
        }
    ) {
      TextField(
        value = searchText,
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
        keyboardActions = KeyboardActions(onSearch = onSearch),
        colors = OutlinedTextFieldDefaults.colors(
          unfocusedBorderColor = Color.Transparent,
          focusedBorderColor = Color.Transparent
        ),
        placeholder = {
          Text(
            text = "search",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        },
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
      )
    }
    Spacer(Modifier.height(8.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
    ) {
      UsageCard(
        label = "7 day average",
        usage = 10000000L,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
      Spacer(modifier = Modifier.width(16.dp))
      UsageCard(
        label = "Today",
        usage = 20000000L,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
    }
    UsageBarGraph()
    DashboardActionBar(onEvent = onEvent)
  }
}