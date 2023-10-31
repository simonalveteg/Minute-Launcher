package com.example.android.minutelauncher.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDAO {

  @Query("SELECT * FROM app")
  fun getAllApps(): Flow<List<App>>

  @Transaction
  @Query("SELECT * FROM SwipeApp")
  fun getGestureApps(): Flow<List<SwipeAppWithApp>>

  @Transaction
  @Query("SELECT * FROM FavoriteApp ORDER BY `order` ASC")
  fun getFavoriteApps(): Flow<List<FavoriteAppWithApp>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertApp(app: App)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertFavoriteApp(app: FavoriteApp)

  @Delete
  fun deleteFavoriteApp(app: FavoriteApp)

  @Transaction
  fun toggleFavoriteApp(app: App) {
    val fApp = getFavoriteById(app.id)
    if (fApp != null) {
      deleteFavoriteApp(fApp)
    } else {
      val order = getMaxFavoriteOrder()
      insertFavoriteApp(FavoriteApp(app.id, order))
    }
  }

  @Query("SELECT 'MAX(order)' FROM FavoriteApp")
  fun getMaxFavoriteOrder(): Int

  @Query("SELECT * FROM FavoriteApp WHERE app_id = :id")
  fun getFavoriteById(id: Int): FavoriteApp?

  @Query("SELECT 'order' FROM FavoriteApp WHERE app_id = :id")
  fun getOrderForFavoriteById(id: Int): Int

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertGestureApp(swipeApp: SwipeApp)

  @Query("DELETE FROM SwipeApp WHERE swipeDirection = :direction")
  fun removeAppForGesture(direction: String)

  @Transaction
  @Query("SELECT * FROM SwipeApp WHERE swipeDirection = :direction")
  fun getAppForGesture(direction: String): SwipeAppWithApp?
}