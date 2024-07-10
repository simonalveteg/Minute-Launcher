package com.alveteg.simon.minutelauncher.home.modal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AccessTimer
import com.alveteg.simon.minutelauncher.data.AccessTimerMapping
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.MinuteBottomSheet
import com.alveteg.simon.minutelauncher.home.SegmentedControl
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerBottomSheet(
  sheetState: SheetState,
  appInfo: AppInfo,
  timerMappings: List<AccessTimerMapping>,
  onDismissRequest: () -> Unit,
  onEvent: (Event) -> Unit
) {
  val hasDefaultTimer = appInfo.app.timer == AccessTimer.DEFAULT
  val selectedTimer by remember(appInfo.app.timer) {
    derivedStateOf { timerMappings.first { it.enum == appInfo.app.timer }.integerValue }.also {
      Timber.d("Derived new state: ${it.value}")
    }
  }

  MinuteBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Change app timer",
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
      SegmentedControl(
        items = timerMappings.map { it.integerValue }.toSortedSet(),
        selectedItem = selectedTimer,
        onItemSelection = { selected ->
          onEvent(
            Event.UpdateApp(
              appInfo.app.copy(timer = timerMappings.first { it.integerValue == selected }.enum)
            )
          )
        }
      )
      TextButton(
        enabled = !hasDefaultTimer,
        onClick = {
          onEvent(Event.UpdateApp(appInfo.app.copy(timer = AccessTimer.DEFAULT)))
        }
      ) {
        val resetText = "Reset to default"
        Text(
          text = resetText.uppercase(),
          style = MaterialTheme.typography.labelMedium,
          fontFamily = archivoFamily,
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}
