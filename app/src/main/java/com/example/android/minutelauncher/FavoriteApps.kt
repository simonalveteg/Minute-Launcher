package com.example.android.minutelauncher

import android.gesture.GestureStore
import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
  val gestureThreshold = 10f
  Surface(Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          forEachGesture {
            detectDragGestures(
              onDragEnd = { gestureEvent?.let { viewModel.onEvent(it) } }
            ) { change, dragAmount ->
              change.consume()
              Log.d("SWIPE", "position: ${change.position}") // height: 0-2399f
              val gestureZone = if (change.position.y < 2399/2) GestureZone.UPPER else GestureZone.LOWER
              gestureHandler(dragAmount, gestureThreshold, gestureZone)?.let { gestureEvent = it }
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