package com.alveteg.simon.minutelauncher.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [
    App::class,
    SwipeApp::class,
    FavoriteApp::class,
    MindfulDelay::class
  ],
  version = 5,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LauncherDatabase : RoomDatabase() {
  abstract fun launcherDao(): LauncherDao

  companion object {

    val MIGRATION_4_5 = object : Migration(4, 5) {
      override fun migrate(db: SupportSQLiteDatabase) {
        // Create the new MindfulDelay table
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `MindfulDelay` (
            `packageName` TEXT NOT NULL, 
            `delay` INTEGER NOT NULL, 
            PRIMARY KEY(`packageName`), 
            FOREIGN KEY(`packageName`) REFERENCES `App`(`packageName`) 
            ON UPDATE NO ACTION ON DELETE CASCADE 
          )
          """.trimIndent()
        )

        // Create index for MindfulDelay
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_MindfulDelay_packageName` ON `MindfulDelay` (`packageName`)")

        // Copy data from AppTimer to MindfulDelay
        db.execSQL(
          """
          INSERT INTO `MindfulDelay` (`packageName`, `delay`)
          SELECT `packageName`, `timer` FROM `AppTimer`
          """.trimIndent()
        )

        // Drop the old AppTimer table
        db.execSQL("DROP TABLE IF EXISTS `AppTimer`")
      }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `App` ADD COLUMN `displayTitle` TEXT DEFAULT NULL")
      }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {

        // Create a temporary table with the NEW schema (no timer column)
        db.execSQL(
          """
          CREATE TABLE `App_new` (
            `packageName` TEXT NOT NULL, 
            `appTitle` TEXT NOT NULL, 
            PRIMARY KEY(`packageName`)
          )
        """
        )

        // Copy the data from the old table to the new one
        db.execSQL(
          """
          INSERT INTO `App_new` (`packageName`, `appTitle`)
          SELECT `packageName`, `appTitle` FROM `App`
        """
        )

        // Remove the old table and rename the new one
        db.execSQL("DROP TABLE `App`")
        db.execSQL("ALTER TABLE `App_new` RENAME TO `App`")

        // --- 2. Create the new AppTimer table ---
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `AppTimer` (
            `packageName` TEXT NOT NULL, 
            `timer` INTEGER NOT NULL, 
            PRIMARY KEY(`packageName`), 
            FOREIGN KEY(`packageName`) REFERENCES `App`(`packageName`) 
            ON UPDATE NO ACTION ON DELETE CASCADE 
          )
        """
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_AppTimer_packageName` ON `AppTimer` (`packageName`)")

        // Drop the old mapping table
        db.execSQL("DROP TABLE IF EXISTS `AccessTimerMapping`")
      }
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
          UPDATE SwipeApp 
          SET swipeDirection = CASE swipeDirection
            WHEN 'UPPER_LEFT'  THEN 'TOP_RIGHT'
            WHEN 'UPPER_RIGHT' THEN 'TOP_LEFT'
            WHEN 'LOWER_LEFT'  THEN 'BOTTOM_RIGHT'
            WHEN 'LOWER_RIGHT' THEN 'BOTTOM_LEFT'
            ELSE swipeDirection
          END
        """
        )
      }
    }
  }
}
