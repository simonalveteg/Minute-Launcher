package com.example.android.minutelauncher

import android.app.Application
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ResolveInfo
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.mutate
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
  private val usageStatsManager by lazy {
    application.applicationContext.getSystemService(
      ComponentActivity.USAGE_STATS_SERVICE
    ) as UsageStatsManager
  }
  private val appList by lazy { queryUsageStats() }
  private val mainIntent = Intent().apply {
    action = Intent.ACTION_MAIN
    addCategory(Intent.CATEGORY_LAUNCHER)
  }

  val searchTerm = mutableStateOf("")

  private val pm = application.applicationContext.packageManager
  private var installedPackages = pm.queryIntentActivities(mainIntent, 0).sortedBy {
    it.loadLabel(pm).toString().lowercase()
  }
  private val installedApps = MutableStateFlow(installedPackages.map { it.toUserApp(pm) })
  var applicationList = MutableStateFlow(installedApps.value)
    private set

  val favoriteApps: Flow<List<UserApp>> = channelFlow {
    application.applicationContext.datastore.data.collectLatest { appSettings ->
      val test = appSettings.favoriteApps
      send(test)
    }
  }

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
        updateSearch("")
      }
      is Event.UpdateSearch -> updateSearch(event.searchTerm)
      is Event.CloseAppsList -> {
        updateSearch("")
        sendUiEvent(UiEvent.HideAppsList)
      }
      is Event.ToggleFavorite -> {
        dismissDialog()
        toggleFavorite(event.app)
      }
      is Event.ShowAppInfo -> sendUiEvent(UiEvent.ShowAppInfo(event.app))
      is Event.DismissDialog -> dismissDialog()
      is Event.SearchClicked -> sendUiEvent(UiEvent.Search)
      is Event.DismissSearch -> sendUiEvent(UiEvent.DismissSearch)
      is Event.SwipeRight -> Unit
      is Event.SwipeLeft -> Unit
      is Event.SwipeUp -> Unit
      is Event.SwipeDown -> Unit
    }
  }

  fun getUsageForApp(app: UserApp) =
    mutableStateOf(appList[app.packageName]?.totalTimeInForeground ?: 0)

  fun getAppTitle(app: ResolveInfo) = mutableStateOf(app.loadLabel(pm).toString())

  fun getTotalUsage() = mutableStateOf(appList.values.sumOf { it.totalTimeInForeground })

  private fun updateSearch(text: String) {
    viewModelScope.apply {
      launch {
        searchTerm.value = text
      }
      launch {
        applicationList.value = installedApps.value.filter {
          it.appTitle
            .replace(" ", "")
            .replace("-", "")
            .contains(text.trim(), true)
        }
      }
    }
  }

  private fun queryUsageStats(): MutableMap<String, UsageStats> {
    val currentTime = System.currentTimeMillis()
    val startTime = Calendar.getInstance()
      .apply {
        timeZone = TimeZone.getDefault()
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
      }.timeInMillis
    return (usageStatsManager.queryAndAggregateUsageStats(startTime, currentTime).filter {
      // TODO: Replace later when implementing app exclusions for usage stats
      application.applicationContext.packageName != it.key
    } as MutableMap<String, UsageStats>)
  }

  private fun toggleFavorite(app: UserApp) {
    viewModelScope.launch {
      application.applicationContext.datastore.updateData {
        it.copy(
          favoriteApps = it.favoriteApps.mutate { list ->
            if (!list.contains(app)) list.add(app)
            else list.remove(app)
            Log.d("vm", app.packageName)
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

  private fun dismissDialog() {
    sendUiEvent(UiEvent.DismissDialog)
  }
}