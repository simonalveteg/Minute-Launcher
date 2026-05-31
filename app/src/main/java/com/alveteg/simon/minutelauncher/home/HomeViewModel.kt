package com.alveteg.simon.minutelauncher.home

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.R
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.data.AppSignals
import com.alveteg.simon.minutelauncher.data.LauncherRepository
import com.alveteg.simon.minutelauncher.data.PreferenceRepository
import com.alveteg.simon.minutelauncher.data.UsageRepository
import com.alveteg.simon.minutelauncher.data.toTimeUsed
import com.alveteg.simon.minutelauncher.settings.SettingsEvent
import com.alveteg.simon.minutelauncher.settings.SettingsScreen
import com.alveteg.simon.minutelauncher.utilities.Gesture
import com.alveteg.simon.minutelauncher.utilities.combine
import com.alveteg.simon.minutelauncher.utilities.filterBySearchTerm
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val roomRepository: LauncherRepository,
  private val usageRepository: UsageRepository,
  private val preferenceRepository: PreferenceRepository,
  @ApplicationContext private val context: Context
) : ViewModel() {

  private val _uiEvent = MutableSharedFlow<UiEvent>()
  val uiEvent = _uiEvent.asSharedFlow().onEach { Timber.d(it.toString()) }

  val transparencyAmount = preferenceRepository.transparencyAmount
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.TRANSPARENCY_AMOUNT
    )

  val mindfulDelayLength = preferenceRepository.mindfulDelayLength
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.MINDFUL_DELAY_LENGTH
    )

  val showDefaultHomePrompt = preferenceRepository.showDefaultHomePrompt
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.SHOW_DEFAULT_HOME_PROMPT
    )

  val showAdminAccessPrompt = preferenceRepository.showAdminAccessPrompt
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.SHOW_ADMIN_ACCESS_PROMPT
    )

  val showUsageAccessPrompt = preferenceRepository.showUsageAccessPrompt
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.SHOW_USAGE_ACCESS_PROMPT
    )

  val skipAppModal = preferenceRepository.skipAppModal
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.SKIP_APP_MODAL
    )

  val showOnboarding = preferenceRepository.showOnboarding
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      PreferenceRepository.Defaults.SHOW_ONBOARDING
    )

  private val _searchTerm = MutableStateFlow("")
  val searchTerm = _searchTerm.asStateFlow()

  private val _appSignals = MutableStateFlow<List<AppSignals>>(emptyList())

  private val _favoriteApps = MutableStateFlow<List<AppInfo>>(emptyList())
  val favoriteApps = _favoriteApps.asStateFlow()

  val installedApps = combine(
    roomRepository.appList(),
    roomRepository.mindfulDelayApps(),
    roomRepository.favoriteApps(),
    usageRepository.usageStats,
    _appSignals,
    mindfulDelayLength
  ) { apps, delayApps, favorites, usageStats, signals, defaultDelayLength ->
    apps.map { app ->
      val favorite = favorites.map { it.app.packageName }.contains(app.packageName)
      val usage = usageStats.filter { app.packageName == it.packageName }
      val delay = delayApps.find { it.app.packageName == app.packageName }?.mindfulDelay?.delay
        ?: defaultDelayLength
      val signal = signals.find { it.packageName == app.packageName }
      AppInfo(
        app = app,
        favorite = favorite,
        mindfulDelay = delay,
        usage = usage,
        isSystemApp = signal?.isSystemApp ?: false,
        isUpdatedSystemApp = signal?.isUpdatedSystemApp ?: false,
        isDefaultBrowser = signal?.isDefaultBrowser ?: false,
        isDefaultSms = signal?.isDefaultSms ?: false,
        isDefaultDialer = signal?.isDefaultDialer ?: false,
        canHandleShare = signal?.canHandleShare ?: false
      )
    }
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val filteredApps = combine(
    installedApps, searchTerm
  ) { apps, searchTerm ->
    apps.filterBySearchTerm(searchTerm)
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  private val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
  private val usageAccessCallback = AppOpsManager.OnOpChangedListener { _, _ ->
    monitorUsage()
  }

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
    usageRepository.registerCallback(packageCallback)
    updateDatabase()
    updateAppSignals()
    monitorUsage()

    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    appOps.startWatchingMode(
      AppOpsManager.OPSTR_GET_USAGE_STATS,
      context.packageName,
      usageAccessCallback
    )

    viewModelScope.launch(Dispatchers.IO) {
      combine(
        roomRepository.favoriteApps(), installedApps
      ) { favorites, apps ->
        favorites.sortedBy { it.favoriteApp.order }.mapNotNull { favorite ->
          apps.find { it.app.packageName == favorite.app.packageName }?.let { app ->
            AppInfo(favorite.app, true, app.mindfulDelay, app.usage)
          }
        }
      }.collect { favorites ->
        Timber.d("Favorite apps updated: ${favorites.size}")
        _favoriteApps.value = favorites
      }
    }
  }

  private fun monitorUsage() {
    if (isUsageAccessGranted(context)) {
      viewModelScope.launch(Dispatchers.IO) {
        usageRepository.startUsageUpdater()
      }
    }
  }

  private fun updateDatabase() {
    Timber.d("Update Database Called")
    viewModelScope.launch(Dispatchers.IO) {
      val currentApps = roomRepository.appList().first()
      val installedApps = usageRepository.getApps()
      val currentAppPackageNames = currentApps.map { it.packageName }.toSet()
      val installedAppPackageNames = installedApps.map { it.packageName }.toSet()
      val newApps = installedApps.filter { !currentAppPackageNames.contains(it.packageName) }
      val removedApps = currentApps.filter { !installedAppPackageNames.contains(it.packageName) }

      Timber.d("${newApps.size} new apps and ${removedApps.size} removed apps found.")

      newApps.forEach { roomRepository.insertApp(it) }
      removedApps.forEach { roomRepository.removeApp(it) }
      updateAppSignals()
    }
  }

  private fun updateAppSignals() {
    viewModelScope.launch(Dispatchers.IO) {
      _appSignals.value = usageRepository.getAppSignals()
    }
  }

  fun onEvent(event: Event) {
    Timber.d(event.toString())
    when (event) {
      is HomeEvent.OpenApplication -> {
        sendUiEvent(UiEvent.ShowModal(event.appInfo))
        sendUiEvent(UiEvent.VibrateLongPress)
      }

      is HomeEvent.LaunchActivity -> {
        val appInfo = event.appInfo
        Timber.d("Launch Activity ${appInfo.app.appTitle}")
        usageRepository.getLaunchIntentForPackage(appInfo.app.packageName)?.let { intent ->
          sendUiEvent(UiEvent.LaunchActivity(intent))
          sendUiEvent(
            UiEvent.ShowToast(
              context.getString(
                R.string.toast_app_usage,
                appInfo.app.appTitle,
                appInfo.usage.firstOrNull()?.usageDuration.toTimeUsed(context, false)
              )
            )
          )
          viewModelScope.launch {
            delay(100)
            _searchTerm.value = ""
          }
        }
      }

      is HomeEvent.UpdateSearch -> {
        Timber.d("Update search with ${event.searchTerm}")
        _searchTerm.value = event.searchTerm
      }

      is HomeEvent.ToggleFavorite -> {
        viewModelScope.launch(Dispatchers.IO) {
          Timber.d("Toggle favorite app ${event.app.appTitle}")
          roomRepository.toggleFavorite(event.app)
        }
      }

      is HomeEvent.HandleGesture -> {
        val gesture = event.gesture
        Timber.d("Gesture handled, $gesture")
        sendUiEvent(UiEvent.VibrateLongPress)
        when (gesture) {
          Gesture.UP -> {
            sendUiEvent(UiEvent.ShowDashboard)
          }

          Gesture.DOWN -> {
            sendUiEvent(UiEvent.ExpandNotifications)
          }

          else -> {
            viewModelScope.launch(Dispatchers.IO) {
              val appInfo = roomRepository.getAppInfoForGesture(gesture)?.let {
                getAppInfoForApp(it.app)
              }
              sendUiEvent(UiEvent.TriggerGesture(gesture, appInfo))
            }
          }
        }
      }

      is HomeEvent.OpenSettings -> sendUiEvent(UiEvent.Navigate(SettingsScreen.HOME))
      is HomeEvent.UpdateFavoriteOrder -> {
        val favorites = _favoriteApps.value.toMutableList()
        val reorderedItem = favorites.removeAt(event.from)
        favorites.add(event.to, reorderedItem)

        _favoriteApps.value = favorites.toList()

        Timber.d("Favorite order updated: $favorites")
        viewModelScope.launch(Dispatchers.IO) {
          roomRepository.updateFavoritesOrder(favorites.map { it.app })
        }
      }

      is HomeEvent.UpdateMindfulDelay -> {
        viewModelScope.launch(Dispatchers.IO) {
          roomRepository.updateMindfulDelay(event.app, event.delayValue)
        }
      }

      is HomeEvent.ResetMindfulDelayToDefault -> {
        viewModelScope.launch(Dispatchers.IO) {
          roomRepository.removeMindfulDelay(event.app)
        }
      }

      is HomeEvent.SetDisplayName -> {
        viewModelScope.launch(Dispatchers.IO) {
          roomRepository.updateApp(event.app.copy(displayTitle = event.displayName))
        }
      }

      is HomeEvent.ResetDisplayName -> {
        viewModelScope.launch(Dispatchers.IO) {
          roomRepository.updateApp(event.app.copy(displayTitle = null))
        }
      }

      is HomeEvent.HideDefaultAppPrompt -> {
        viewModelScope.launch(Dispatchers.IO) {
          preferenceRepository.setShowDefaultHomePrompt(false)
        }
      }

      is HomeEvent.HideUsageAccessPrompt -> {
        viewModelScope.launch(Dispatchers.IO) {
          preferenceRepository.setShowUsageAccessPrompt(false)
        }
      }

      is HomeEvent.HideAdminAccessPrompt -> {
        viewModelScope.launch(Dispatchers.IO) {
          preferenceRepository.setShowAdminAccessPrompt(false)
        }
      }

      is HomeEvent.HideOnboarding -> {
        viewModelScope.launch(Dispatchers.IO) {
          preferenceRepository.updateShowOnboarding(false)
        }
      }

      is HomeEvent.ShowToast -> sendUiEvent(UiEvent.ShowToast(event.text))

      is SettingsEvent.OpenGestureList -> sendUiEvent(UiEvent.Navigate(SettingsScreen.GESTURE_SETTINGS_LIST + "/${event.gesture}"))
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
    appOps.stopWatchingMode(usageAccessCallback)
    usageRepository.unregisterCallback()
    super.onCleared()
  }
}
