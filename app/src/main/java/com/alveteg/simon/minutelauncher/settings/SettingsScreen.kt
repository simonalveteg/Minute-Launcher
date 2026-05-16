package com.alveteg.simon.minutelauncher.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.settings.components.GenericInput
import com.alveteg.simon.minutelauncher.settings.components.GestureInput
import com.alveteg.simon.minutelauncher.settings.components.SegmentedInput
import com.alveteg.simon.minutelauncher.settings.components.SliderInput
import com.alveteg.simon.minutelauncher.settings.components.settingsSection
import com.alveteg.simon.minutelauncher.theme.AppTheme
import com.alveteg.simon.minutelauncher.utilities.Gesture
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  onNavigate: (UiEvent.Navigate) -> Unit,
  viewModel: SettingsViewModel = hiltViewModel()
) {
  val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
  val useDynamicColor by viewModel.useDynamicColor.collectAsStateWithLifecycle()
  val transparencyAmount by viewModel.transparencyAmount.collectAsStateWithLifecycle()
  val timerLength by viewModel.timerLength.collectAsStateWithLifecycle()
  val gestureApps by viewModel.gestureApps.collectAsState(initial = emptyMap())
  val appsWithTimers by viewModel.appsWithTimers.map { it.sortedBy { it.app.appTitle.lowercase() } }
    .collectAsState(initial = emptyList())

  var _transparencyAmount by remember(transparencyAmount) { mutableFloatStateOf(transparencyAmount) }
  val transparencyAmountLabel by remember(_transparencyAmount) {
    derivedStateOf { (_transparencyAmount * 100).roundToInt().toString() + "%" }
  }

  var _timerLength by remember(timerLength) { mutableIntStateOf(timerLength) }
  val timerLengthLabel by remember(_timerLength) {
    derivedStateOf { "${_timerLength}s" }
  }

  LaunchedEffect(key1 = true) {
    viewModel.uiEvent.collect { event ->
      when (event) {
        is UiEvent.Navigate -> onNavigate(event)
        else -> Unit
      }
    }
  }

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        title = {
          Text("Minute Launcher Settings")
        },
        navigationIcon = {
          IconButton(
            onClick = { onNavigate(UiEvent.Navigate(SettingsScreen.BACK, popBackStack = true)) }
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Navigate back."
            )
          }
        },
        scrollBehavior = scrollBehavior
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .padding(padding),
      horizontalAlignment = Alignment.Start
    ) {
      settingsSection(title = "Preferences") {
        item {
          SliderInput(
            label = "Background Transparency",
            description = "Choose how transparent the color layer above your background picture should be. Default is 50%",
            value = _transparencyAmount,
            valueLabel = transparencyAmountLabel,
            valueRange = 0f..1f,
            roundToInt = false,
            onValueChangeFinished = { viewModel.onTransparencyAmountChange(_transparencyAmount) },
            onValueChange = { _transparencyAmount = it }
          )
        }
      }

      settingsSection(title = "Theme and Colors") {
        item {
          SegmentedInput(
            label = "App Theme",
            description = "Choose whether the app should be in Light, Dark, or follow System settings.",
            options = AppTheme.entries,
            selectedOption = appTheme,
            onOptionSelect = { viewModel.onThemeChange(it) },
            labelProvider = { it.label }
          )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          item {
            SegmentedInput(
              label = "Color Palette",
              description = "Choose whether to use the default theme or colors generated from your wallpaper (Material You).",
              options = listOf(false, true),
              selectedOption = useDynamicColor,
              onOptionSelect = { viewModel.onDynamicColorChange(it) },
              labelProvider = {
                if (it) "Dynamic" else "Default"
              }
            )
          }
        }
      }

      settingsSection(
        title = "Gestures",
        description = "Choose which apps to open when swiping on the home screen."
      ) {
        item {
          GestureInput(
            gesture = Gesture.TOP_LEFT,
            app = gestureApps[Gesture.TOP_LEFT],
            iconResource = R.drawable.gesture_top_left,
            onEvent = viewModel::onEvent
          )
          GestureInput(
            gesture = Gesture.TOP_RIGHT,
            app = gestureApps[Gesture.TOP_RIGHT],
            iconResource = R.drawable.gesture_top_right,
            onEvent = viewModel::onEvent
          )
          GestureInput(
            gesture = Gesture.BOTTOM_LEFT,
            app = gestureApps[Gesture.BOTTOM_LEFT],
            iconResource = R.drawable.gesture_bottom_left,
            onEvent = viewModel::onEvent
          )
          GestureInput(
            gesture = Gesture.BOTTOM_RIGHT,
            app = gestureApps[Gesture.BOTTOM_RIGHT],
            iconResource = R.drawable.gesture_bottom_right,
            onEvent = viewModel::onEvent
          )
        }
      }

      settingsSection(
        title = "Mindful Delay",
        description = "Add a short pause before apps open to help you stay intentional. This delay gives you a moment to decide if you really want to spend time on the app."
      ) {
        item {
          SliderInput(
            label = "Default Delay Length",
            description = "Choose how many seconds you need to wait before being allowed to open an app. Default is 5 seconds.",
            value = _timerLength.toFloat(),
            valueLabel = timerLengthLabel,
            valueRange = 0f..16f,
            steps = 15,
            roundToInt = true,
            onValueChangeFinished = { viewModel.onTimerLengthChange(_timerLength) },
            onValueChange = { _timerLength = it.roundToInt() }
          )
        }

        item {
          GenericInput(
            label = "Apps with custom timers set",
            description = "View and reset the timers for apps that have one set."
          )
        }
        items(
          items = appsWithTimers,
          key = { it.app.packageName }
        ) { item ->
          val index = appsWithTimers.indexOf(item)
          val isFirst = index == 0
          val isLast = index == appsWithTimers.lastIndex

          val shape = when {
            isFirst && isLast -> MaterialTheme.shapes.medium
            isFirst -> MaterialTheme.shapes.medium.copy(
              bottomStart = CornerSize(0.dp),
              bottomEnd = CornerSize(0.dp)
            )

            isLast -> MaterialTheme.shapes.medium.copy(
              topStart = CornerSize(0.dp),
              topEnd = CornerSize(0.dp)
            )

            else -> RectangleShape
          }

          Surface(
            modifier = Modifier
              .animateItem(),
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp)
                .height(IntrinsicSize.Min),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = item.app.appTitle)
              Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
              )
              {
                Text(text = "${item.appTimer.timer}s")
                VerticalDivider(
                  modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                )
                IconButton(
                  onClick = { viewModel.onEvent(HomeEvent.ResetAppTimerToDefault(item.app)) }
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Reset Timer",
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}