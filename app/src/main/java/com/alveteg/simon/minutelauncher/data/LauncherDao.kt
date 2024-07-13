package com.alveteg.simon.minutelauncher.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Dao
interface LauncherDao {

  @Query("SELECT * FROM app")
  fun getAllApps(): Flow<List<App>>

  @Transaction
  @Query("SELECT * FROM SwipeApp")
  fun getGestureApps(): Flow<List<SwipeAppWithApp>>

  @Transaction
  @Query("SELECT * FROM FavoriteApp ORDER BY `order` ASC")
  fun getFavoriteApps(): Flow<List<FavoriteAppWithApp>>

  @Query("SELECT * FROM App WHERE packageName = :packageName")
  fun getAppById(packageName: String): App?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertApp(app: App)

  @Delete
  fun removeApp(app: App)

  @Query("UPDATE App SET timer = :timer WHERE packageName = :packageName")
  fun updateAppTimer(packageName: String, timer: AccessTimer)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertFavoriteApp(app: FavoriteApp)

  @Query("UPDATE FavoriteApp SET `order` = :order WHERE packageName = :packageName")
  fun updateFavoriteOrder(packageName: String, order: Int)

  @Delete
  fun deleteFavoriteApp(app: FavoriteApp)

  @Transaction
  fun toggleFavoriteApp(app: App) {
    val fApp = getFavoriteById(app.packageName)
    if (fApp != null) {
      deleteFavoriteApp(fApp)
    } else {
      val order = getMaxFavoriteOrder() + 1
      Timber.d("Insert new favorite at position: $order")
      insertFavoriteApp(FavoriteApp(app.packageName, order))
    }
  }

  @Query("SELECT MAX(`order`) FROM FavoriteApp")
  fun getMaxFavoriteOrder(): Int

  @Query("SELECT * FROM FavoriteApp WHERE packageName = :packageName")
  fun getFavoriteById(packageName: String): FavoriteApp?


  @Query("SELECT 'order' FROM FavoriteApp WHERE packageName = :packageName")
  fun getOrderForFavoriteById(packageName: String): Int

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertGestureApp(swipeApp: SwipeApp)

  @Query("DELETE FROM SwipeApp WHERE swipeDirection = :direction")
  fun removeAppForGesture(direction: String)

  @Transaction
  @Query("SELECT * FROM SwipeApp WHERE swipeDirection = :direction")
  fun getAppForGesture(direction: String): SwipeAppWithApp?
}