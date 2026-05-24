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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.PreferenceRepository
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.settings.components.SliderInput
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerBottomSheet(
  sheetState: SheetState,
  appInfo: AppInfo,
  defaultTimerLength: Int,
  onDismissRequest: () -> Unit,
  onEvent: (Event) -> Unit
) {
  var _timerLength by remember(appInfo.timer) { mutableFloatStateOf(appInfo.timer.toFloat()) }
  val timerLengthLabel by remember(_timerLength) {
    derivedStateOf { _timerLength.roundToInt().toString() + "s" }
  }

  MinuteBottomSheet(
    onDismissRequest = onDismissRequest, sheetState = sheetState,
    title = "Mindful Delay",
    description = "Set how long you want to wait before being able to open this app. A longer delay helps you pause and break the habit of mindless clicking."
  ) {
    SliderInput(
      value = _timerLength,
      valueLabel = timerLengthLabel,
      valueRange = PreferenceRepository.Defaults.MIN_TIMER.toFloat()..PreferenceRepository.Defaults.MAX_TIMER.toFloat(),
      steps = PreferenceRepository.Defaults.MAX_TIMER,
      roundToInt = true,
      onValueChangeFinished = {
        onEvent(HomeEvent.UpdateAppTimer(appInfo.app, _timerLength.roundToInt()))
      },
      onValueChange = { _timerLength = it }
    )
    TextButton(
      onClick = {
        onEvent(HomeEvent.ResetAppTimerToDefault(appInfo.app))
      },
      enabled = appInfo.timer != defaultTimerLength,
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
