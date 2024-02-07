package com.alveteg.simon.minutelauncher.data

import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.alveteg.simon.minutelauncher.utilities.Gesture

@Entity
data class App(
  @PrimaryKey(autoGenerate = true) val id: Int,
  @ColumnInfo(name = "package_name") val packageName: String,
  @ColumnInfo(name = "app_title") val appTitle: String,
  @ColumnInfo(name = "timer") val timer: Int = 5,
  @ColumnInfo(name = "installed") val installed: Boolean = true
)

fun LauncherActivityInfo.toApp() =
  App(
    this.applicationInfo.packageName.hashCode(),
    this.applicationInfo.packageName,
    this.label.toString(),
  )

@Entity(
  foreignKeys = [
    ForeignKey(
      entity = App::class,
      parentColumns = ["id"],
      childColumns = ["app_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["app_id"])]
)
data class SwipeApp(
  @PrimaryKey val swipeDirection: Gesture,
  @ColumnInfo(name = "app_id") val appId: Int
) {
  constructor(gesture: Gesture, app: App) : this(gesture, app.id)
}

data class SwipeAppWithApp(
  @Embedded val swipeApp: SwipeApp,
  @Relation(
    parentColumn = "app_id",
    entityColumn = "id"
  )
  val app: App
)


@Entity(
  primaryKeys = ["app_id"],
  foreignKeys = [ForeignKey(
    entity = App::class,
    parentColumns = ["id"],
    childColumns = ["app_id"],
    onDelete = ForeignKey.CASCADE
  )]
)
data class FavoriteApp(
  @ColumnInfo(name = "app_id") val appId: Int,
  val order: Int
)

data class FavoriteAppWithApp(
  @Embedded val favoriteApp: FavoriteApp,
  @Relation(
    parentColumn = "app_id",
    entityColumn = "id"
  )
  val app: App
)
