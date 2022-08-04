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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
  private val application: Application,
) : ViewModel() {
  private val _uiEvent = Channel<UiEvent>()
  val uiEvent = _uiEvent.receiveAsFlow()
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
      val test = appSettings.favoriteApps.mapNotNull { app ->
        val intent = Intent().apply {
          setPackage(app.packageName)
          action = Intent.ACTION_MAIN
        }
        pm.resolveActivity(intent, 0)?.let {
          return@mapNotNull it.toUserApp(pm)
        }
      }
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
        viewModelScope.launch {
          toggleFavorite(event.app)
        }
      }
      is Event.ShowAppInfo -> sendUiEvent(UiEvent.ShowAppInfo(event.app))
      is Event.DismissDialog -> dismissDialog()
    }
  }

  fun getUsageForApp(app: UserApp) =
    mutableStateOf(appList.find { it.packageName == app.packageName }?.totalTimeInForeground ?: 0)

  fun getAppTitle(app: ResolveInfo) = mutableStateOf(app.loadLabel(pm).toString())

  fun getTotalUsage() = mutableStateOf(appList.sumOf { it.totalTimeInForeground })

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

  private fun queryUsageStats(): MutableList<UsageStats> {
    val currentTime = System.currentTimeMillis()
    val startTime = Calendar.getInstance()
      .apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
      }.timeInMillis
    return usageStatsManager.queryUsageStats(
      UsageStatsManager.INTERVAL_DAILY,
      startTime,
      currentTime
    )
  }

  private suspend fun toggleFavorite(app: UserApp) {
    application.applicationContext.datastore.updateData {
      it.copy(
        favoriteApps = it.favoriteApps.mutate { list ->
          if (!list.contains(app)) list.add(app)
          else list.remove(app)
        }
      )
    }
  }

  private fun sendUiEvent(event: UiEvent) {
    viewModelScope.launch {
      _uiEvent.send(event)
    }
  }

  private fun dismissDialog() {
    sendUiEvent(UiEvent.DismissDialog)
  }
}