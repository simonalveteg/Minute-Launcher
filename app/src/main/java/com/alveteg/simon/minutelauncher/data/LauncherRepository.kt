package com.alveteg.simon.minutelauncher.data

import com.alveteg.simon.minutelauncher.utilities.Gesture
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class LauncherRepository @Inject constructor(
  private val launcherDao: LauncherDAO,
  private val accessTimerMappingDao: AccessTimerMappingDao
) {

  fun appList() = launcherDao.getAllApps()
  fun gestureApps() =
    launcherDao.getGestureApps()
      .map { it.associate { gApp -> gApp.swipeApp.swipeDirection to gApp.app } }

  fun favoriteApps() = launcherDao.getFavoriteApps()
  fun insertApp(app: App) = launcherDao.insertApp(app)
  fun updateApp(app: App) {
    launcherDao.updateAppTimer(app.id, app.timer)
  }

  fun removeApp(app: App) = launcherDao.removeApp(app)

  fun getAppById(id: Int) = launcherDao.getAppById(id)
  fun toggleFavorite(app: App) = launcherDao.toggleFavoriteApp(app)
  fun insertGestureApp(swipeApp: SwipeApp) {
    launcherDao.removeAppForGesture(swipeApp.swipeDirection.toString())
    launcherDao.insertGestureApp(swipeApp)
  }

  fun removeAppForGesture(gesture: Gesture) = launcherDao.removeAppForGesture(gesture.toString())
  fun getAppInfoForGesture(gesture: Gesture) = launcherDao.getAppForGesture(gesture.toString())
  fun updateFavoritesOrder(new: List<FavoriteAppInfo>) {
    new.forEachIndexed { index, appInfo ->
      Timber.d(
        "Updating order for: ${appInfo.favoriteApp.app.appTitle} from ${
          launcherDao.getOrderForFavoriteById(
            appInfo.favoriteApp.app.id
          )
        } to $index"
      )
      launcherDao.updateFavoriteOrder(appInfo.favoriteApp.app.id, index)
    }
  }

  fun getAccessTimerMappings() = accessTimerMappingDao.getAllMappings()
  fun setAccessTimerMapping(accessTimerMapping: AccessTimerMapping) =
    accessTimerMappingDao.insertMapping(accessTimerMapping)
}
