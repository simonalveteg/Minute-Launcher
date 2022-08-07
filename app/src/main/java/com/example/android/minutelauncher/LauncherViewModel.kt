package com.example.android.minutelauncher

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

  var uiState by mutableStateOf(
    UiState(
      apps = installedApps,
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
    private set

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
      is Event.CloseAppsList -> {
        updateSearch("")
        sendUiEvent(UiEvent.HideAppsList)
      }
      is Event.OpenAppsList -> {
        sendUiEvent(UiEvent.ShowAppsList)
        sendUiEvent(UiEvent.Search)
      }
      is Event.ToggleFavorite -> {
        sendUiEvent(UiEvent.DismissDialog)
        toggleFavorite(event.app)
      }
      is Event.ShowAppInfo -> sendUiEvent(UiEvent.ShowAppInfo(event.app))
      is Event.DismissDialog -> sendUiEvent(UiEvent.DismissDialog)
      is Event.SearchClicked -> sendUiEvent(UiEvent.Search)
      is Event.DismissSearch -> sendUiEvent(UiEvent.DismissSearch)
      is Event.SwipeRight -> Unit
      is Event.SwipeLeft -> Unit
      is Event.SwipeUp -> onEvent(Event.OpenAppsList)
      is Event.SwipeDown -> sendUiEvent(UiEvent.ShowNotifications)
    }
  }

  fun getUsageForApp(app: UserApp) =
    mutableStateOf(uiState.usage[app.packageName] ?: 0)

  fun getTotalUsage() = mutableStateOf(uiState.usage.values.sum())

  private fun updateSearch(text: String) {
    viewModelScope.apply {
      uiState = uiState.copy(
        filteredApps = installedApps.filter {
          it.appTitle
            .replace(" ", "")
            .replace("-", "")
            .contains(text.trim(), true)
        },
        searchTerm = text
      )
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
    val apps: List<UserApp>,
    val filteredApps: List<UserApp>,
    val favoriteApps: Flow<List<UserApp>>,
    val usage: Map<String, Long>,
    val searchTerm: String = ""
  )
}