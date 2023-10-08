package com.example.android.minutelauncher.db

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.android.minutelauncher.GestureDirection

@Entity
data class App(
  @PrimaryKey(autoGenerate = true) val id: Int, // hashcode of package name
  val packageName: String,
  val appTitle: String
)
fun ResolveInfo.toApp(packageManager: PackageManager) =
  App(
    this.activityInfo.packageName.hashCode(),
    this.activityInfo.packageName,
    this.loadLabel(packageManager).toString()
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
  @PrimaryKey val swipeDirection: GestureDirection,
  @ColumnInfo(name = "app_id") val appId: Int
) {
  constructor(gesture: GestureDirection, app: App): this(gesture, app.id)
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
  @ColumnInfo(name = "app_id") val appId: Int
)

data class FavoriteAppWithApp(
  @Embedded val favoriteApp: FavoriteApp,
  @Relation(
    parentColumn = "app_id",
    entityColumn = "id"
  )
  val app: App
)
