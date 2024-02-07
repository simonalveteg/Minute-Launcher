package com.alveteg.simon.minutelauncher.data

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableLongStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.home.ScreenState
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
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

  private val _currentModalId = MutableStateFlow<Int?>(null)
  private val _currentModal = MutableStateFlow<App?>(null)
  val currentModal = _currentModal.asStateFlow()

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
    }.sortedBy { it.appTitle.lowercase() }
  }

  private val handler = Handler(Looper.getMainLooper())
  private var usageQueryRunnable = Runnable { handlerTest() }

  private val _screenState = MutableStateFlow(ScreenState.FAVORITES)
  val screenState = _screenState.asStateFlow()

  init {
    Timber.d("INIT.")
    handler.removeCallbacksAndMessages(null)
    usageQueryRunnable.run()
    updateDatabase()
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        _currentModalId.collect { appModalId ->
          Timber.d("CurrentModalId: $appModalId")
          _currentModal.value = if (appModalId == null) null else repo.getAppById(appModalId)
        }
      }
    }
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
      is Event.UpdateFavoriteOrder -> updateFavoriteOrder(event.favorites)
      is Event.ChangeScreenState -> _screenState.value = event.state
      is Event.UpdateApp -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            repo.updateApp(event.app)
          }
        }
      }

      is Event.ClearModal -> _currentModalId.value = null
    }
  }

  private fun updateFavoriteOrder(favorites: List<FavoriteAppWithApp>) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        repo.updateFavoritesOrder(favorites)
      }
    }
  }

  fun getUsageForApp(app: App) =
    mutableLongStateOf(usageStats.value[app.packageName] ?: 0)

  fun getTotalUsage() = mutableLongStateOf(usageStats.value.values.sum())

  private fun handleGesture(gesture: Gesture) {
    if (!screenState.value.isFavorites()) return
    var vibrate = true
    Timber.d("Gesture handled, $gesture")
    when (gesture) {
      Gesture.UP -> _screenState.value = ScreenState.APPS
      Gesture.DOWN -> sendUiEvent(UiEvent.ExpandNotifications)
      else -> {
        vibrate = false
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            repo.getAppForGesture(gesture)?.let {
              openApplication(it.app)
            }
          }
        }
      }
    }
    if (vibrate) sendUiEvent(UiEvent.VibrateLongPress)
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
    usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, currentTime)
      .sortedBy {
        it.totalTimeInForeground
      }.also {
        Timber.d("Usage queried for ${it.size} applications.")
      }

    return usageStatsManager.queryAndAggregateUsageStats(startTime, currentTime)
      .filter { application.applicationContext.packageName != it.key }
      .mapValues { it.value.totalTimeInForeground }
  }

  private fun setGestureApp(app: App, gesture: Gesture) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        repo.insertGestureApp(SwipeApp(gesture, app))
      }
    }
  }

  private fun clearGestureApp(gesture: Gesture) {
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
    _currentModalId.value = app.id
    sendUiEvent(UiEvent.VibrateLongPress)
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