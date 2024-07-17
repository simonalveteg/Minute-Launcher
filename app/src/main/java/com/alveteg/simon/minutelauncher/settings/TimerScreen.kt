package com.alveteg.simon.minutelauncher.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.AccessTimer
import com.alveteg.simon.minutelauncher.data.AccessTimerMapping
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
  onNavigate: (UiEvent.Navigate) -> Unit,
  viewModel: SettingsViewModel = hiltViewModel()
) {
  LaunchedEffect(key1 = true) {
    Timber.d("launched effect")
    viewModel.uiEvent.collect { event ->
      Timber.d("event: $event")
      when (event) {
        is UiEvent.Navigate -> onNavigate(event)
        else -> Unit
      }
    }
  }

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val accessTimerMapping by viewModel.accessTimerMappings.collectAsState(initial = emptyList())
  val defaultTimerApps by viewModel.defaultTimerApps.collectAsState(initial = emptyList())
  val nonDefaultTimerApps by viewModel.nonDefaultTimerApps.collectAsState(initial = emptyList())
  val defaultMapping = accessTimerMapping.firstOrNull { it.enum == AccessTimer.DEFAULT }
  val mappings = accessTimerMapping
    .filter { it.enum != AccessTimer.DEFAULT }
    .sortedBy { it.integerValue }

  Surface {
    Scaffold(
      topBar = {
        LargeTopAppBar(
          title = {
            Text(
              text = "Timer Settings",
              fontFamily = archivoBlackFamily
            )
          },
          navigationIcon = {
            IconButton(onClick = { onNavigate(UiEvent.Navigate(SettingsScreen.HOME, true)) }) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate Back"
              )
            }
          },
          scrollBehavior = scrollBehavior
        )
      }
    ) { paddingValues ->
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .nestedScroll(scrollBehavior.nestedScrollConnection)
          .padding(paddingValues)
          .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        item {
          defaultMapping?.let {
            val atm = mappings.firstOrNull { it.integerValue == defaultMapping.integerValue }
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
              modifier = Modifier.fillMaxWidth(),
              expanded = expanded,
              onExpandedChange = { expanded = !expanded }
            ) {
              Surface {
                OutlinedTextField(
                  readOnly = true,
                  value = "Default timer length: ${atm?.enum?.name}",
                  onValueChange = {},
                  trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                  },
                  colors = OutlinedTextFieldDefaults.colors(),
                  modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                )
              }
              ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
              ) {
                mappings.forEach {
                  DropdownMenuItem(
                    text = { Text(text = "${it.enum.name} (${it.integerValue}s)") },
                    onClick = {
                      expanded = false
                      viewModel.onEvent(
                        SettingsEvent.SetDefaultTimer(
                          AccessTimerMapping(
                            enum = AccessTimer.DEFAULT,
                            integerValue = it.integerValue
                          )
                        )
                      )
                    }
                  )
                }
              }
            }
          }
        }
        item {
          Spacer(modifier = Modifier.height(20.dp))
        }
        items(nonDefaultTimerApps) { app ->
          AppTimerCard(app, accessTimerMapping, viewModel::onEvent)
        }
        item {
          Spacer(modifier = Modifier.height(20.dp))
        }
        items(defaultTimerApps, key = { it.app.packageName }) { app ->
          AppTimerCard(app, accessTimerMapping, viewModel::onEvent)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimerCard(
  appInfo: AppInfo,
  timerMappings: List<AccessTimerMapping>,
  onEvent: (Event) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  Surface(
    modifier = Modifier.padding(vertical = 1.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = appInfo.app.appTitle,
        fontFamily = archivoFamily,
        fontSize = 18.sp,
        overflow = TextOverflow.Clip,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
          .weight(1f)
      ) {
        OutlinedTextField(
          readOnly = true,
          value = appInfo.app.timer.name,
          onValueChange = {},
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
          },
          colors = OutlinedTextFieldDefaults.colors(),
          modifier = Modifier
            .menuAnchor()
            .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          timerMappings.forEach {
            DropdownMenuItem(
              text = { Text(text = "${it.enum.name} (${it.integerValue}s)") },
              onClick = {
                expanded = false
                onEvent(
                  HomeEvent.UpdateApp(
                    appInfo.app.copy(timer = it.enum)
                  )
                )
              }
            )
          }
        }
      }
    }
  }
}