package com.example.android.minutelauncher

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableLongStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.minutelauncher.db.App
import com.example.android.minutelauncher.db.LauncherRepository
import com.example.android.minutelauncher.db.SwipeApp
import com.example.android.minutelauncher.db.toApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
  private val application: Application,
  private val repo: LauncherRepository
) : ViewModel() {
  private val _uiEvent = MutableSharedFlow<UiEvent>()
  val uiEvent = _uiEvent.asSharedFlow()
    .onEach { Timber.d(it.toString()) }

  private val pm = application.applicationContext.packageManager

  private val _usageStats = MutableStateFlow(queryUsageStats())
  val usageStats = _usageStats.asStateFlow()

  private val _searchTerm = MutableStateFlow("")
  val searchTerm = _searchTerm.asStateFlow()

  val gestureApps = repo.gestureApps()
  val favoriteApps = repo.favoriteApps()
  val filteredApps = combine(
    repo.appList(),
    searchTerm
  ) { apps, searchTerm ->
    apps.filter { app ->
      app.appTitle.lowercase().filterNot { it.isWhitespace() }.contains(
        searchTerm.lowercase().filterNot { it.isWhitespace() }
      )
    }.sortedBy { it.appTitle }
  }

  private val handler = Handler(Looper.getMainLooper())
  private var usageQueryRunnable = Runnable { handlerTest() }

  init {
    Timber.d("INIT.")
    handler.removeCallbacksAndMessages(null)
    usageQueryRunnable.run()
    updateDatabase()
  }

  private fun updateDatabase() {
    val installedApps = pm.queryIntentActivities(
      Intent().apply { action = Intent.ACTION_MAIN; addCategory(Intent.CATEGORY_LAUNCHER) },
      0
    ).sortedBy { it.loadLabel(pm).toString().lowercase() }.map { it.toApp(pm) }
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        installedApps.forEach { app ->
          repo.insertApp(app)
        }
      }
    }
  }

  fun onEvent(event: Event) {
    Timber.d(event.toString())
    when (event) {
      is Event.OpenApplication -> openApplication(event.app)
      is Event.LaunchActivity -> launchActivity(event.app)
      is Event.UpdateSearch -> updateSearch(event.searchTerm)
      is Event.ToggleFavorite -> toggleFavorite(event.app)
      is Event.HandleGesture -> handleGesture(event.gesture)
      is Event.SetAppGesture -> setGestureApp(event.app, event.gesture)
      is Event.ClearAppGesture -> clearGestureApp(event.gesture)
    }
  }

  fun getUsageForApp(app: App) =
    mutableLongStateOf(usageStats.value[app.packageName] ?: 0)

  fun getTotalUsage() = mutableLongStateOf(usageStats.value.values.sum())

  private fun handleGesture(gestureDirection: GestureDirection) {
    Timber.d("Gesture handled, $gestureDirection")
    when (gestureDirection) {
      GestureDirection.UP -> sendUiEvent(UiEvent.OpenAppDrawer)
      GestureDirection.DOWN -> sendUiEvent(UiEvent.ExpandNotifications)
      else -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            repo.getAppForGesture(gestureDirection)?.let {
              openApplication(it.app)
            }
          }
        }
      }
    }
  }

  private fun updateSearch(text: String?) {
    Timber.d("Update search with $text")
    _searchTerm.value = text ?: ""
  }

  private fun handlerTest() {
    Timber.d("Usage queried.")
    _usageStats.value = queryUsageStats()
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
        Timber.d(
          "Package: ${it.packageName} time: ${it.firstTimeStamp}, usage: ${it.totalTimeInForeground.toTimeUsed()}"
        )
      }

    return usageStatsManager.queryAndAggregateUsageStats(startTime, currentTime)
      .filter { application.applicationContext.packageName != it.key }
      .mapValues { it.value.totalTimeInForeground }
  }

  private fun setGestureApp(app: App, gesture: GestureDirection) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        repo.insertGestureApp(SwipeApp(gesture, app))
      }
    }
  }

  private fun clearGestureApp(gesture: GestureDirection) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        repo.removeAppForGesture(gesture)
      }
    }
  }

  private fun toggleFavorite(app: App) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        Timber.d("Toggle favorite app ${app.appTitle}")
        repo.toggleFavorite(app)
      }
    }
  }

  private fun openApplication(app: App) {
    Timber.d("Open Application ${app.appTitle}")
    sendUiEvent(UiEvent.OpenApplication(app))
  }

  private fun launchActivity(app: App) {
    Timber.d("Launch Activity ${app.appTitle}")
    pm.getLaunchIntentForPackage(app.packageName)?.apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    }?.let { intent ->
      sendUiEvent(UiEvent.LaunchActivity(intent))
      sendUiEvent(
        UiEvent.ShowToast(
          "${app.appTitle} used for ${getUsageForApp(app).longValue.toTimeUsed(false)}"
        )
      )
      viewModelScope.launch { delay(100); updateSearch("") }
    }
  }

  private fun sendUiEvent(event: UiEvent) {
    viewModelScope.launch {
      _uiEvent.emit(event)
    }
  }
}