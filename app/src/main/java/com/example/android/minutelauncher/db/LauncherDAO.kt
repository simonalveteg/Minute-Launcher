package com.example.android.minutelauncher.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Dao
interface LauncherDAO {

  @Query("SELECT * FROM app")
  fun getAllApps(): Flow<List<App>>

  @Transaction
  @Query("SELECT * FROM SwipeApp")
  fun getGestureApps(): Flow<List<SwipeAppWithApp>>

  @Transaction
  @Query("SELECT * FROM FavoriteApp")
  fun getFavoriteApps(): Flow<List<FavoriteAppWithApp>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertApp(app: App)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertFavoriteApp(app: FavoriteApp)

  @Delete
  fun deleteFavoriteApp(app: FavoriteApp)

  @Transaction
  fun toggleFavoriteApp(app: App) {
    if (isFavoriteApp(app.id)) {
      deleteFavoriteApp(FavoriteApp(app.id))
    } else {
      insertFavoriteApp(FavoriteApp(app.id))
    }
  }

  @Query("SELECT * FROM FavoriteApp WHERE app_id = :id")
  fun isFavoriteApp(id: Int): Boolean

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertGestureApp(swipeApp: SwipeApp)

  @Query("DELETE FROM SwipeApp WHERE swipeDirection = :direction")
  fun removeAppForGesture(direction: String)

  @Transaction
  @Query("SELECT * FROM SwipeApp WHERE swipeDirection = :direction")
  fun getAppForGesture(direction: String): SwipeAppWithApp?

}