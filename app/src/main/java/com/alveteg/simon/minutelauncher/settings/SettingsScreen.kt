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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.PreferenceRepository
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.settings.components.GenericColumnInput
import com.alveteg.simon.minutelauncher.settings.components.GestureInput
import com.alveteg.simon.minutelauncher.settings.components.SegmentedInput
import com.alveteg.simon.minutelauncher.settings.components.SliderInput
import com.alveteg.simon.minutelauncher.settings.components.ToggleInput
import com.alveteg.simon.minutelauncher.settings.components.settingsSection
import com.alveteg.simon.minutelauncher.theme.AppTheme
import com.alveteg.simon.minutelauncher.utilities.Gesture
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
  val skipAppModal by viewModel.skipAppModal.collectAsStateWithLifecycle()
  val mindfulDelayLength by viewModel.mindfulDelayLength.collectAsStateWithLifecycle()
  val gestureApps by viewModel.gestureApps.collectAsState(initial = emptyMap())
  val appsWithMindfulDelay by viewModel.appsWithMindfulDelay.collectAsState(initial = emptyList())
  val appsWithMindfulDelaySorted = remember {
    appsWithMindfulDelay.sortedBy { it.app.appTitle.lowercase() }
  }

  var _transparencyAmount by remember(transparencyAmount) { mutableFloatStateOf(transparencyAmount) }
  val transparencyAmountLabel by remember(_transparencyAmount) {
    derivedStateOf { (_transparencyAmount * 100).roundToInt().toString() + "%" }
  }

  var _mindfulDelayLength by remember(mindfulDelayLength) { mutableIntStateOf(mindfulDelayLength) }
  val mindfulDelayLengthLabel by remember(_mindfulDelayLength) {
    derivedStateOf { "${_mindfulDelayLength}s" }
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
          Text(stringResource(R.string.settings_title))
        },
        navigationIcon = {
          IconButton(
            onClick = { onNavigate(UiEvent.Navigate(SettingsScreen.BACK, popBackStack = true)) }
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = null
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
        .padding(horizontal = 24.dp)
        .padding(padding),
      horizontalAlignment = Alignment.Start
    ) {

      item {
        PermissionCheckers(
          onEvent = viewModel::onEvent,
          showDismissButton = false,
        )
      }

      settingsSection(
        title = R.string.section_title_gestures,
        description = R.string.section_description_gestures
      ) {
        items(Gesture.getHorizontalGestures()) {
          GestureInput(
            gesture = it,
            app = gestureApps[it],
            iconResource = it.getIcon(),
            onEvent = viewModel::onEvent
          )
        }
      }

      settingsSection(
        title = R.string.title_mindful_delay,
        description = R.string.description_mindful_delay
      ) {
        item {
          SliderInput(
            label = stringResource(R.string.label_default_delay_length),
            description = stringResource(R.string.description_default_delay_length),
            value = _mindfulDelayLength.toFloat(),
            valueLabel = mindfulDelayLengthLabel,
            valueRange = PreferenceRepository.Defaults.MIN_MINDFUL_DELAY.toFloat() .. PreferenceRepository.Defaults.MAX_MINDFUL_DELAY.toFloat(),
            steps = PreferenceRepository.Defaults.MAX_MINDFUL_DELAY,
            roundToInt = true,
            onValueChangeFinished = { viewModel.onMindfulDelayLengthChange(_mindfulDelayLength) },
            onValueChange = { _mindfulDelayLength = it.roundToInt() }
          )
        }
        item {
          ToggleInput(
            label = stringResource(R.string.label_auto_open),
            description = stringResource(R.string.description_auto_open),
            checked = skipAppModal,
            onCheckedChange = { viewModel.onSkipAppModalChange(it) },
          )
        }

        item {
          if (appsWithMindfulDelaySorted.isNotEmpty()) {
            GenericColumnInput(
              label = stringResource(R.string.label_apps_with_custom_delays),
              description = stringResource(R.string.description_apps_with_custom_delays),
              modifier = Modifier.padding(top = 16.dp)
            )
          }
        }
        items(
          items = appsWithMindfulDelaySorted,
          key = { it.app.packageName }
        ) { item ->
          val index = appsWithMindfulDelaySorted.indexOf(item)
          val isFirst = index == 0
          val isLast = index == appsWithMindfulDelaySorted.lastIndex

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
                Text(text = "${item.mindfulDelay.delay}s")
                VerticalDivider(
                  modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                )
                IconButton(
                  onClick = { viewModel.onEvent(HomeEvent.ResetMindfulDelayToDefault(item.app)) }
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = stringResource(R.string.label_reset_to_default),
                  )
                }
              }
            }
          }
        }
      }

      settingsSection(title = R.string.section_title_appearance) {
        item {
          SegmentedInput(
            label = stringResource(R.string.label_app_theme),
            description = stringResource(R.string.description_app_theme),
            options = AppTheme.entries,
            selectedOption = appTheme,
            onOptionSelect = { viewModel.onThemeChange(it) },
            labelProvider = { stringResource(it.labelRes) }
          )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          item {
            SegmentedInput(
              label = stringResource(R.string.label_color_palette),
              description = stringResource(R.string.description_color_palette),
              options = listOf(false, true),
              selectedOption = useDynamicColor,
              onOptionSelect = { viewModel.onDynamicColorChange(it) },
              labelProvider = {
                if (it) stringResource(R.string.color_palette_dynamic) else stringResource(R.string.color_palette_default)
              }
            )
          }
        }
        item {
          SliderInput(
            label = stringResource(R.string.label_background_transparency),
            description = stringResource(R.string.description_background_transparency),
            value = _transparencyAmount,
            valueLabel = transparencyAmountLabel,
            valueRange = PreferenceRepository.Defaults.MIN_TRANSPARENCY .. PreferenceRepository.Defaults.MAX_TRANSPARENCY,
            roundToInt = true,
            steps = 19,
            onValueChangeFinished = { viewModel.onTransparencyAmountChange(_transparencyAmount) },
            onValueChange = { _transparencyAmount = it }
          )
        }
      }
    }
  }
}