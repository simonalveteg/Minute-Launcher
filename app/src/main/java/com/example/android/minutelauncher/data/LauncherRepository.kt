package com.example.android.minutelauncher.data

import com.example.android.minutelauncher.utilities.Gesture
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

  fun getAppById(id: Int) = dao.getAppById(id)
  fun toggleFavorite(app: App) = dao.toggleFavoriteApp(app)
  fun insertGestureApp(swipeApp: SwipeApp) {
    dao.removeAppForGesture(swipeApp.swipeDirection.toString())
    dao.insertGestureApp(swipeApp)
  }

  fun removeAppForGesture(gesture: Gesture) = dao.removeAppForGesture(gesture.toString())
  fun getAppForGesture(gesture: Gesture) = dao.getAppForGesture(gesture.toString())
  fun updateFavoritesOrder(new: List<FavoriteAppWithApp>) {
    new.forEachIndexed { index, app ->
      Timber.d("Updating order for: ${app.app.appTitle} from ${dao.getOrderForFavoriteById(app.app.id)} to $index")
      dao.updateFavoriteOrder(app.favoriteApp.appId, index)
    }
  }
}
