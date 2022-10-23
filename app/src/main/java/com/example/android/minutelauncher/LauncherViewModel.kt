package com.example.android.minutelauncher

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
  private val application: Application,
) : ViewModel() {
  private val _uiEvent = MutableSharedFlow<UiEvent>()
  val uiEvent = _uiEvent.asSharedFlow()
    .onSubscription { Log.d("UI_EVENT", "New subscriber") }
    .onCompletion { Log.d("UI_EVENT", "Completed") }
    .onEach { Log.d("UI_EVENT", it.toString()) }

  private val pm = application.applicationContext.packageManager
  private val installedApps = pm.queryIntentActivities(
    Intent().apply { action = Intent.ACTION_MAIN; addCategory(Intent.CATEGORY_LAUNCHER) },
    0
  ).sortedBy { it.loadLabel(pm).toString().lowercase() }.map { it.toUserApp(pm) }

  private val _uiState = MutableStateFlow(
    UiState(
      installedApps = installedApps,
      filteredApps = installedApps,
      gestureApps = channelFlow {
        application.applicationContext.datastore.data.collectLatest { appSettings ->
          send(appSettings.gestureApps)
        }
      },
      favoriteApps = channelFlow {
        application.applicationContext.datastore.data.collectLatest { appSettings ->
          send(appSettings.favoriteApps)
        }
      },
      usage = queryUsageStats()
    )
  )
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private val handler = Handler(Looper.getMainLooper())
  private var usageQueryRunnable = Runnable { handlerTest() }

  init {
    Log.d("VIEW_MODEL", "INIT.")
    handler.removeCallbacksAndMessages(null)
    usageQueryRunnable.run()
  }

  fun onEvent(event: Event) {
    Log.d("VIEW_MODEL", event.toString())
    when (event) {
      is Event.OpenApplication -> {
        openApplication(event.app)
      }
      is Event.UpdateSearch -> updateSearch(event.searchTerm)
      is Event.ToggleFavorite -> toggleFavorite(event.app)
      is Event.HandleGesture -> handleGesture(event.gesture)
      is Event.SetAppGesture -> setGestureApp(event.app, event.gesture)
      is Event.ClearAppGesture -> clearGestureApp(event.gesture)
    }
  }

  fun getUsageForApp(app: UserApp) =
    mutableStateOf(uiState.value.usage[app.packageName] ?: 0)

  fun getTotalUsage() = mutableStateOf(uiState.value.usage.values.sum())

  private fun handleGesture(gestureDirection: GestureDirection) {
    Log.d("VIEW_MODEL","Gesture handled, $gestureDirection")
    when (gestureDirection) {
      GestureDirection.UP -> sendUiEvent(UiEvent.OpenAppDrawer)
      GestureDirection.DOWN -> Unit
      else -> viewModelScope.launch {
        application.applicationContext.datastore.data.collectLatest { appSettings ->
          appSettings.gestureApps[gestureDirection]?.let { openApplication(it) }
          cancel()
        }
      }
    }
  }

  private fun updateSearch(text: String?) {
    Log.d("VIEW_MODEL", "Update search with $text")
    val textString = text ?: ""
    viewModelScope.apply {
      _uiState.update { it ->
        it.copy(
          filteredApps = installedApps.filter { app ->
            app.appTitle
              .replace(" ", "")
              .replace("-", "")
              .contains(textString.trim(), true)
          },
          searchTerm = textString
        )
      }
    }
  }

  private fun handlerTest() {
    Log.d("VIEW_MODEL", "Usage queried.")
    _uiState.update { it.copy(usage = queryUsageStats()) }
    handler.postDelayed(usageQueryRunnable, 60000)
  }

  private fun queryUsageStats(): Map<String, Long> {
    val usageStatsManager = application.applicationContext
      .getSystemService(ComponentActivity.USAGE_STATS_SERVICE) as UsageStatsManager
    val currentTime = System.currentTimeMillis()
    val startTime = Calendar.getInstance()
      .apply {
        timeZone = TimeZone.getDefault()
        set(Calendar.HOUR_OF_DAY, 4)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
      }.timeInMillis
    val yesterday = Calendar.getInstance()
      .apply {
        timeZone = TimeZone.getDefault()
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
      }.timeInMillis
    usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, currentTime)
      .sortedBy {
      it.totalTimeInForeground
    }.forEach {
      Log.d(
        "VIEW_MODEL",
        "Package: ${it.packageName} time: ${it.firstTimeStamp}, usage: ${it.totalTimeInForeground.toTimeUsed()}"
      )
    }

    return usageStatsManager.queryAndAggregateUsageStats(startTime, currentTime)
      .filter { application.applicationContext.packageName != it.key }
      .mapValues { it.value.totalTimeInForeground }
  }

  private fun setGestureApp(app: UserApp, gesture: GestureDirection) {
    viewModelScope.launch {
      application.applicationContext.datastore.updateData {
        it.copy(
          gestureApps = it.gestureApps.mutate { map ->
            if(map.contains(gesture)) {
              map.replace(gesture, app)
            } else {
              map[gesture] = app
            }
          }
        )
      }
    }
  }

  private fun clearGestureApp(gesture: GestureDirection) {
    viewModelScope.launch {
      application.applicationContext.datastore.updateData {
        it.copy(
          gestureApps = it.gestureApps.mutate { map ->
            map.remove(gesture)
          }
        )
      }
    }
  }

  private fun toggleFavorite(app: UserApp) {
    viewModelScope.launch {
      application.applicationContext.datastore.updateData {
        it.copy(
          favoriteApps = it.favoriteApps.mutate { list ->
            if (!list.contains(app)) list.add(app)
            else list.remove(app)
            Log.d("VIEW_MODEL", "Toggled favorite: ${app.packageName}")
          }
        )
      }
    }
  }

  private fun openApplication(app: UserApp) {
    Log.d("VIEW_MODEL","Open Application ${app.appTitle}")
    sendUiEvent(UiEvent.ShowToast(app.appTitle))
    pm.getLaunchIntentForPackage(app.packageName)?.apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    }?.let {
      sendUiEvent(UiEvent.StartActivity(it))
    }
    viewModelScope.launch { delay(100); updateSearch("") }
  }

  private fun sendUiEvent(event: UiEvent) {
    viewModelScope.launch {
      _uiEvent.emit(event)
    }
  }

  data class UiState(
    val installedApps: List<UserApp>,
    val filteredApps: List<UserApp>,
    val favoriteApps: Flow<List<UserApp>>,
    val gestureApps: Flow<Map<GestureDirection, UserApp>>,
    val usage: Map<String, Long>,
    val searchTerm: String = ""
  )
}