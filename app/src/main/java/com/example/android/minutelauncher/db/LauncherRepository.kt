package com.example.android.minutelauncher.db

import com.example.android.minutelauncher.GestureDirection
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LauncherRepository @Inject constructor(private val dao: LauncherDAO) {

  fun appList() = dao.getAllApps()
  fun gestureApps() =
    dao.getGestureApps().map { it.associate { gApp -> gApp.swipeApp.swipeDirection to gApp.app } }

  fun favoriteApps() = dao.getFavoriteApps()

  fun insertApp(app: App) = dao.insertApp(app)
  fun toggleFavorite(app: App) = dao.toggleFavoriteApp(app)
  fun insertGestureApp(swipeApp: SwipeApp) = dao.insertGestureApp(swipeApp)
  fun removeAppForGesture(gesture: GestureDirection) = dao.removeAppForGesture(gesture.toString())
  fun getAppForGesture(gesture: GestureDirection) = dao.getAppForGesture(gesture.toString())
}
