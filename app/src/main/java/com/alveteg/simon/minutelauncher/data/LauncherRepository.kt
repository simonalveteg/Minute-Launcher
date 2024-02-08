package com.alveteg.simon.minutelauncher.data

import com.alveteg.simon.minutelauncher.utilities.Gesture
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class LauncherRepository @Inject constructor(private val dao: LauncherDAO) {

  fun appList() = dao.getAllApps()
  fun gestureApps() =
    dao.getGestureApps().map { it.associate { gApp -> gApp.swipeApp.swipeDirection to gApp.app } }

  fun favoriteApps() = dao.getFavoriteApps()
  fun insertApp(app: App) = dao.insertApp(app)
  fun updateApp(app: App) {
    dao.updateAppTimer(app.id, app.timer)
  }
  fun removeApp(app: App) = dao.removeApp(app)

  fun getAppById(id: Int) = dao.getAppById(id)
  fun toggleFavorite(app: App) = dao.toggleFavoriteApp(app)
  fun insertGestureApp(swipeApp: SwipeApp) {
    dao.removeAppForGesture(swipeApp.swipeDirection.toString())
    dao.insertGestureApp(swipeApp)
  }

  fun removeAppForGesture(gesture: Gesture) = dao.removeAppForGesture(gesture.toString())
  fun getAppInfoForGesture(gesture: Gesture) = dao.getAppForGesture(gesture.toString())
  fun updateFavoritesOrder(new: List<FavoriteAppInfo>) {
    new.forEachIndexed { index, appInfo ->
      Timber.d(
        "Updating order for: ${appInfo.favoriteApp.app.appTitle} from ${
          dao.getOrderForFavoriteById(
            appInfo.favoriteApp.app.id
          )
        } to $index"
      )
      dao.updateFavoriteOrder(appInfo.favoriteApp.app.id, index)
    }
  }
}
