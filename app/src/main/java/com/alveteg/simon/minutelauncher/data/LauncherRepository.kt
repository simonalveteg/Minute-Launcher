package com.alveteg.simon.minutelauncher.data

import com.alveteg.simon.minutelauncher.utilities.Gesture
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class LauncherRepository @Inject constructor(
  private val launcherDao: LauncherDao,
) {

  fun appList() = launcherDao.getAllApps()
  fun gestureApps() =
    launcherDao.getGestureApps()
      .map { it.associate { gApp -> gApp.swipeApp.swipeDirection to gApp.app } }

  fun favoriteApps() = launcherDao.getFavoriteApps()
  fun mindfulDelayApps() = launcherDao.getAppsWithMindfulDelay()
  fun insertApp(app: App) = launcherDao.insertApp(app)
  fun updateApp(app: App) = launcherDao.updateApp(app)
  fun updateMindfulDelay(app: App, delayValue: Int) {
    launcherDao.updateMindfulDelay(app.packageName, delayValue)
  }
  fun removeMindfulDelay(app: App) = launcherDao.removeMindfulDelay(app.packageName)

  fun removeApp(app: App) = launcherDao.removeApp(app)

  fun toggleFavorite(app: App) = launcherDao.toggleFavoriteApp(app)
  fun insertGestureApp(swipeApp: SwipeApp) {
    launcherDao.removeAppForGesture(swipeApp.swipeDirection.toString())
    launcherDao.insertGestureApp(swipeApp)
  }

  fun removeAppForGesture(gesture: Gesture) = launcherDao.removeAppForGesture(gesture.toString())
  fun getAppInfoForGesture(gesture: Gesture) = launcherDao.getAppForGesture(gesture.toString())
  fun updateFavoritesOrder(new: List<App>) {
    new.forEachIndexed { index, app ->
      Timber.d(
        "Updating order for: ${app.appTitle} from ${
          launcherDao.getOrderForFavoriteById(
            app.packageName
          )
        } to $index"
      )
      launcherDao.updateFavoriteOrder(app.packageName, index)
    }
  }

}
