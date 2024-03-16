package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.home.MinuteBottomSheet
import com.alveteg.simon.minutelauncher.home.SegmentedControl
import com.alveteg.simon.minutelauncher.home.stats.UsageBarGraph
import com.alveteg.simon.minutelauncher.home.stats.UsageCard
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.clearFocusOnKeyboardDismiss

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBottomSheet(
  searchText: String,
  onSearch: KeyboardActionScope.() -> Unit,
  onEvent: (Event) -> Unit,
  onGloballyPositioned: (LayoutCoordinates) -> Unit = {}
) {
  val focusRequester = remember { FocusRequester() }
  var gestureSheet by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .navigationBarsPadding()
      .padding(horizontal = 16.dp)
      .padding(bottom = 8.dp, top = 16.dp)
  ) {
    Surface(
      shape = MaterialTheme.shapes.large,
      tonalElevation = 8.dp,
      modifier = Modifier
        .navigationBarsPadding()
        .onGloballyPositioned { onGloballyPositioned(it) }
    ) {
      TextField(
        value = searchText,
        onValueChange = { onEvent(Event.UpdateSearch(it)) },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequester)
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
    DashboardActionBar(
      onOpenGestureSheet = { gestureSheet = true }
    )
  }

  if (gestureSheet) {
    MinuteBottomSheet(
      onDismissRequest = { gestureSheet = false }
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Set gesture shortcuts",
          style = MaterialTheme.typography.headlineSmall,
          fontFamily = archivoBlackFamily,
          modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
        )
        Text(
          text = "The app timer decides how long you need to wait before being able to open the app. ",
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          fontFamily = archivoFamily,
          modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
      }
    }
  }

}