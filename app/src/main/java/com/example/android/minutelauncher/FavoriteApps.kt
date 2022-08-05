package com.example.android.minutelauncher

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.abs

@Composable
fun FavoriteApps(
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val totalUsage by viewModel.getTotalUsage()
  val favorites by viewModel.favoriteApps.collectAsState(initial = emptyList())
  var gestureEvent: Event? = null
  Surface(Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectDragGestures(
            onDragEnd = { gestureEvent?.let { viewModel.onEvent(it) } }
          ) { change, dragAmount ->
            change.consume()
            val threshold = 10f
            if (abs(dragAmount.y) < threshold) {
              if (dragAmount.x > threshold) gestureEvent = Event.SwipeRight
              else if (dragAmount.x < -threshold) gestureEvent = Event.SwipeLeft
            } else {
              if (dragAmount.y > threshold) gestureEvent = Event.SwipeDown
              else if (dragAmount.y < -threshold) gestureEvent = Event.SwipeUp
            }
          }
        },
      verticalArrangement = Arrangement.Bottom,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(totalUsage.toTimeUsed())
      favorites.forEach { app ->
        val appUsage by viewModel.getUsageForApp(app)
        AppCard(
          app.appTitle,
          appUsage,
          { viewModel.onEvent(Event.ShowAppInfo(app)) }
        ) { viewModel.onEvent(Event.OpenApplication(app)) }
      }
      Spacer(modifier = Modifier.height(150.dp)) // TODO: Don't use hardcoded dp value
    }
  }
}