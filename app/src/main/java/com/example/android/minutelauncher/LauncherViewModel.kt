package com.example.android.minutelauncher

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
      favoriteApps = channelFlow {
        application.applicationContext.datastore.data.collectLatest { appSettings ->
          val test = appSettings.favoriteApps
          send(test)
        }
      },
      usage = queryUsageStats()
    )
  )
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()


  fun onEvent(event: Event) {
    Log.d("VIEWMODEL", event.toString())
    when (event) {
      is Event.OpenApplication -> {
        sendUiEvent(UiEvent.ShowToast(event.app.appTitle))
        pm.getLaunchIntentForPackage(event.app.packageName)?.apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }?.let {
          sendUiEvent(UiEvent.StartActivity(it))
        }
        viewModelScope.launch { delay(100); updateSearch("") }
      }
      is Event.UpdateSearch -> updateSearch(event.searchTerm)
      is Event.ToggleFavorite -> toggleFavorite(event.app)
      is Event.HandleGesture -> handleGestureAction(event.gesture)
    }
  }

  private fun handleGestureAction(gestureAction: GestureAction) {
    when (gestureAction.direction) {
      GestureDirection.LEFT -> Unit
      GestureDirection.RIGHT -> Unit
      GestureDirection.UP -> sendUiEvent(UiEvent.OpenAppDrawer)
      GestureDirection.DOWN -> Unit
    }
  }

  fun getUsageForApp(app: UserApp) =
    mutableStateOf(uiState.value.usage[app.packageName] ?: 0)

  fun getTotalUsage() = mutableStateOf(uiState.value.usage.values.sum())

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

  private fun queryUsageStats(): Map<String, Long> {
    val usageStatsManager = application.applicationContext
      .getSystemService(ComponentActivity.USAGE_STATS_SERVICE) as UsageStatsManager
    val currentTime = System.currentTimeMillis()
    val startTime = Calendar.getInstance()
      .apply {
        timeZone = TimeZone.getDefault()
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
      }.timeInMillis
    return usageStatsManager.queryAndAggregateUsageStats(startTime, currentTime)
      .filter { application.applicationContext.packageName != it.key }
      .mapValues { it.value.totalTimeInForeground }
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

  private fun sendUiEvent(event: UiEvent) {
    viewModelScope.launch {
      _uiEvent.emit(event)
    }
  }

  data class UiState(
    val installedApps: List<UserApp>,
    val filteredApps: List<UserApp>,
    val favoriteApps: Flow<List<UserApp>>,
    val usage: Map<String, Long>,
    val searchTerm: String = ""
  )
}