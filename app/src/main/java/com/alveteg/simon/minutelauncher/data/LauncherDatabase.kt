package com.alveteg.simon.minutelauncher.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
  entities = [
    App::class,
    SwipeApp::class,
    FavoriteApp::class,
    AccessTimerMapping::class
  ],
  version = 12,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LauncherDatabase : RoomDatabase() {
  abstract fun launcherDao(): LauncherDao
  abstract fun accessTimerMappingDao(): AccessTimerMappingDao
}