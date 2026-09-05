package com.alveteg.simon.minutelauncher.data

import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.alveteg.simon.minutelauncher.utilities.Gesture
import java.time.LocalDate

@Entity
data class App(
  @PrimaryKey val packageName: String,
  val appTitle: String,
  val category: AppCategory = AppCategory.UNDEFINED,
  val displayTitle: String? = null
) {
  val title: String
    get() = displayTitle ?: appTitle

  companion object {
    val EMPTY = App("", "")
  }
}

enum class AppCategory {
  ACCESSIBILITY, AUDIO, GAME, IMAGE, MAPS, NEWS, PRODUCTIVITY, SOCIAL, VIDEO, UNDEFINED;

  companion object {
    fun fromInt(category: Int): AppCategory {
      return when (category) {
        ApplicationInfo.CATEGORY_ACCESSIBILITY -> ACCESSIBILITY
        ApplicationInfo.CATEGORY_AUDIO -> AUDIO
        ApplicationInfo.CATEGORY_GAME -> GAME
        ApplicationInfo.CATEGORY_IMAGE -> IMAGE
        ApplicationInfo.CATEGORY_MAPS -> MAPS
        ApplicationInfo.CATEGORY_NEWS -> NEWS
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> PRODUCTIVITY
        ApplicationInfo.CATEGORY_SOCIAL -> SOCIAL
        ApplicationInfo.CATEGORY_VIDEO -> VIDEO
        else -> UNDEFINED
      }
    }
  }
}

fun LauncherActivityInfo.toApp() =
  App(
    packageName = this.applicationInfo.packageName,
    category = AppCategory.fromInt(this.applicationInfo.category),
    appTitle = this.label.toString()
  )

/**
 * Stores specific MindfulDelay overrides for apps.
 * If an app is not in this table, it uses the global default from DataStore.
 */
@Entity(
  foreignKeys = [
    ForeignKey(
      entity = App::class,
      parentColumns = ["packageName"],
      childColumns = ["packageName"],
      onDelete = CASCADE
    )
  ],
  indices = [Index(value = ["packageName"])]
)
data class MindfulDelay(
  @PrimaryKey val packageName: String,
  val delay: Int
)

@Entity(
  foreignKeys = [
    ForeignKey(
      entity = App::class,
      parentColumns = ["packageName"],
      childColumns = ["packageName"],
      onDelete = CASCADE
    )
  ],
  indices = [Index(value = ["packageName"])]
)
data class SwipeApp(
  @PrimaryKey val swipeDirection: Gesture,
  val packageName: String
) {
  constructor(gesture: Gesture, app: App) : this(gesture, app.packageName)
}

data class SwipeAppWithApp(
  @Embedded val swipeApp: SwipeApp,
  @Relation(
    parentColumn = "packageName",
    entityColumn = "packageName"
  )
  val app: App
)

@Entity(
  primaryKeys = ["packageName"],
  foreignKeys = [ForeignKey(
    entity = App::class,
    parentColumns = ["packageName"],
    childColumns = ["packageName"],
    onDelete = CASCADE
  )]
)
data class FavoriteApp(
  val packageName: String,
  val order: Int
)

data class FavoriteAppWithApp(
  @Embedded val favoriteApp: FavoriteApp,
  @Relation(
    parentColumn = "packageName",
    entityColumn = "packageName"
  )
  val app: App
)

data class MindfulDelayAppWithApp(
  @Embedded val mindfulDelay: MindfulDelay,
  @Relation(
    parentColumn = "packageName",
    entityColumn = "packageName"
  )
  val app: App
)