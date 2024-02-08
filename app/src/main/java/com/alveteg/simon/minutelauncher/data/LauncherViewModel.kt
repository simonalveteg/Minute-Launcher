package com.alveteg.simon.minutelauncher.data

import android.content.pm.LauncherApps
import android.os.UserHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.home.ScreenState
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.filterBySearchTerm
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
  private val roomRepository: LauncherRepository,
  private val packageRepository: PackageRepository,
  private val usageRepository: UsageRepository
) : ViewModel() {

  private val _uiEvent = MutableSharedFlow<UiEvent>()
  val uiEvent = _uiEvent.asSharedFlow().onEach { Timber.d(it.toString()) }

  private val _searchTerm = MutableStateFlow("")
  val searchTerm = _searchTerm.asStateFlow()

  private val usageStats = usageRepository.queryUsageStats()
  val totalUsage = usageStats.map { it.values.sum() }

  val gestureApps = roomRepository.gestureApps()
  val favoriteApps = combine(
    roomRepository.favoriteApps(), usageStats
  ) { favorites, usageStats ->
    favorites.map {
      val usage = usageStats.getOrDefault(it.app.packageName, 0L)
      FavoriteAppInfo(it, usage)
    }
  }
  private val installedApps = combine(
    roomRepository.appList(), roomRepository.favoriteApps(), usageStats,
  ) { apps, favorites, usageStats ->
    apps.map { app ->
      val favorite = favorites.map { it.app.packageName }.contains(app.packageName)
      val usage = usageStats.getOrDefault(app.packageName, 0L)
      AppInfo(app, favorite, usage)
    }
  }
  val filteredApps = combine(
    installedApps, searchTerm
  ) { apps, searchTerm ->
    apps.filterBySearchTerm(searchTerm)
  }

  private val _screenState = MutableStateFlow(ScreenState.FAVORITES)
  val screenState = _screenState.asStateFlow()

  private val packageCallback = object : LauncherApps.Callback() {
    override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
      Timber.d("Package Removed")
      updateDatabase()
    }

    override fun onPackageAdded(packageName: String?, user: UserHandle?) {
      Timber.d("Package Added")
      updateDatabase()
    }

    override fun onPackageChanged(packageName: String?, user: UserHandle?) {
      Timber.d("Package Changed")
      updateDatabase()
    }

    override fun onPackagesAvailable(
      p1: Array<out String>?, p2: UserHandle?, p3: Boolean
    ) {
    }

    override fun onPackagesUnavailable(
      p1: Array<out String>?, p2: UserHandle?, p3: Boolean
    ) {
    }
  }

  init {
    packageRepository.registerCallback(packageCallback)
    updateDatabase()
  }

  private fun updateDatabase() {
    Timber.d("Update Database Called")
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val currentApps = roomRepository.appList().first()
        val installedApps = packageRepository.getPackages()
        val currentAppPackageNames = currentApps.map { it.packageName }.toSet()
        val installedAppPackageNames = installedApps.map { it.packageName }.toSet()
        val newApps = installedApps.filter { !currentAppPackageNames.contains(it.packageName) }
        val removedApps = currentApps.filter { !installedAppPackageNames.contains(it.packageName) }

        Timber.d("${newApps} new apps and ${removedApps} removed apps found.")

        newApps.forEach { roomRepository.insertApp(it) }
        removedApps.forEach { roomRepository.removeApp(it) }
      }
    }
  }

  fun onEvent(event: Event) {
    Timber.d(event.toString())
    when (event) {
      is Event.OpenApplication -> {
        sendUiEvent(UiEvent.ShowModal(event.appInfo))
        sendUiEvent(UiEvent.VibrateLongPress)
      }

      is Event.LaunchActivity -> {
        val appInfo = event.appInfo
        Timber.d("Launch Activity ${appInfo.app.appTitle}")
        packageRepository.getLaunchIntentForPackage(appInfo.app.packageName)?.let { intent ->
          sendUiEvent(UiEvent.LaunchActivity(intent))
          sendUiEvent(
            UiEvent.ShowToast("${appInfo.app.appTitle} used for ${appInfo.usage.toTimeUsed(false)}")
          )
          viewModelScope.launch {
            delay(100)
            _searchTerm.value = ""
          }
        }
      }

      is Event.UpdateSearch -> {
        Timber.d("Update search with ${event.searchTerm}")
        _searchTerm.value = event.searchTerm
      }

      is Event.ToggleFavorite -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            Timber.d("Toggle favorite app ${event.app.appTitle}")
            roomRepository.toggleFavorite(event.app)
          }
        }
      }

      is Event.HandleGesture -> {
        val gesture = event.gesture
        if (!screenState.value.isFavorites()) return
        Timber.d("Gesture handled, $gesture")
        when (gesture) {
          Gesture.UP -> {
            _screenState.value = ScreenState.APPS
            sendUiEvent(UiEvent.VibrateLongPress)
          }
          Gesture.DOWN -> {
            sendUiEvent(UiEvent.ExpandNotifications)
            sendUiEvent(UiEvent.VibrateLongPress)
          }
          else -> {
            viewModelScope.launch {
              withContext(Dispatchers.IO) {
                roomRepository.getAppInfoForGesture(gesture)?.let {
                  getAppInfoForApp(it.app)?.let { app ->
                    sendUiEvent(UiEvent.ShowModal(app))
                    sendUiEvent(UiEvent.VibrateLongPress)
                  }
                }
              }
            }
          }
        }
      }

      is Event.SetAppGesture -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.insertGestureApp(SwipeApp(event.gesture, event.app))
          }
        }
      }

      is Event.ClearAppGesture -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.removeAppForGesture(event.gesture)
          }
        }
      }

      is Event.UpdateFavoriteOrder -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.updateFavoritesOrder(event.favorites)
          }
        }
      }

      is Event.ChangeScreenState -> _screenState.value = event.state
      is Event.UpdateApp -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            roomRepository.updateApp(event.app)
          }
        }
      }
    }
  }

  private fun sendUiEvent(event: UiEvent) {
    viewModelScope.launch {
      _uiEvent.emit(event)
    }
  }

  private suspend fun getAppInfoForApp(app: App): AppInfo? {
    return installedApps.first().find { it.app.packageName == app.packageName }
  }

  override fun onCleared() {
    packageRepository.unregisterCallback()
    super.onCleared()
  }
}