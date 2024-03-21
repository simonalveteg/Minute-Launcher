package com.alveteg.simon.minutelauncher.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.MinuteRoute
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.LauncherViewModel
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.Gesture
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GestureScreen(
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
  val gestureApps by viewModel.gestureApps.collectAsState(initial = emptyMap())

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Scaffold(
      topBar = {
        LargeTopAppBar(
          title = {
            Text(
              text = "Gesture Settings",
              fontFamily = archivoBlackFamily
            )
          },
          navigationIcon = {
            IconButton(onClick = { onNavigate(UiEvent.Navigate(MinuteRoute.GESTURES, true)) }) {
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
          .padding(horizontal = 16.dp)
      ) {
        stickyHeader {
          Text(
            text = "UPPER HALF OF SCREEN",
            fontFamily = archivoFamily,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
        item {
          GestureEntry(
            name = "Swipe Left",
            gesture = Gesture.UPPER_LEFT,
            app = gestureApps[Gesture.UPPER_LEFT],
            onEvent = viewModel::onEvent
          )
          GestureEntry(
            name = "Swipe Right",
            gesture = Gesture.UPPER_RIGHT,
            app = gestureApps[Gesture.UPPER_RIGHT],
            onEvent = viewModel::onEvent
          )
        }
        stickyHeader {
          Text(
            text = "LOWER HALF OF SCREEN",
            fontFamily = archivoFamily,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
        item {
          GestureEntry(
            name = "Swipe Left",
            gesture = Gesture.LOWER_LEFT,
            app = gestureApps[Gesture.LOWER_LEFT],
            onEvent = viewModel::onEvent
          )
          GestureEntry(
            name = "Swipe Right",
            gesture = Gesture.LOWER_RIGHT,
            app = gestureApps[Gesture.LOWER_RIGHT],
            onEvent = viewModel::onEvent
          )
        }
      }
    }
  }
}

@Composable
fun GestureEntry(
  name: String,
  gesture: Gesture,
  app: App?,
  onEvent: (Event) -> Unit
) {
  val appTitle = app?.appTitle ?: "No app selected"
  Surface(
    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
    tonalElevation = 4.dp,
    shape = MaterialTheme.shapes.medium
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .padding(start = 16.dp, end = 8.dp)
    ) {
      Text(
        text = name,
        fontFamily = archivoFamily
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        FilledTonalButton(
          onClick = {
            onEvent(Event.OpenGestureList(gesture))
          },
          modifier = Modifier.weight(1f),
          shape = MaterialTheme.shapes.medium
        ) {
          Text(text = appTitle)
        }
        IconButton(
          onClick = {
            onEvent(Event.ClearAppGesture(gesture))
          },
          modifier = Modifier.wrapContentWidth()
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Unset Gesture")
        }
      }
    }
  }
}