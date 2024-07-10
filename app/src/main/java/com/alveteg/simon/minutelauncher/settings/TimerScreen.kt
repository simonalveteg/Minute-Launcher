package com.alveteg.simon.minutelauncher.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.MinuteRoute
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.AccessTimer
import com.alveteg.simon.minutelauncher.data.AccessTimerMapping
import com.alveteg.simon.minutelauncher.data.LauncherViewModel
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
  onNavigate: (UiEvent.Navigate) -> Unit,
  viewModel: LauncherViewModel = hiltViewModel()
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
            IconButton(onClick = { onNavigate(UiEvent.Navigate(MinuteRoute.HOME, true)) }) {
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
                Text(
                  text = atm?.enum?.name ?: "",
                  modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                )
              }
              ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = !expanded }
              ) {
                mappings.forEach {
                  DropdownMenuItem(
                    text = { Text(text = it.enum.name) },
                    onClick = {
                      viewModel.onEvent(
                        Event.SetDefaultTimer(
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
        items(mappings) { mapping ->
          AccessTimerMappingCard(mapping = mapping)
        }
      }
    }
  }
}

@Composable
private fun AccessTimerMappingCard(
  mapping: AccessTimerMapping
) {
  Surface(
    modifier = Modifier.fillMaxWidth()
  ) {
    Column {
      Text(text = mapping.enum.name)
      Text(text = mapping.integerValue.toString())
    }
  }
}
