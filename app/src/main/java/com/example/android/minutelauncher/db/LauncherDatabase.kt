package com.example.android.minutelauncher.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
  entities = [
    App::class,
    SwipeApp::class,
    FavoriteApp::class
  ],
  version = 4,
  exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LauncherDatabase : RoomDatabase() {
  abstract fun launcherDao(): LauncherDAO
}