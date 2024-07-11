package com.alveteg.simon.minutelauncher.data

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
  val timer: AccessTimer = AccessTimer.DEFAULT
)

fun LauncherActivityInfo.toApp() =
  App(
    this.applicationInfo.packageName,
    this.label.toString()
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

@Entity(primaryKeys = ["enum"])
data class AccessTimerMapping(
  @ColumnInfo(name = "enum") val enum: AccessTimer,
  @ColumnInfo(name = "integerValue") val integerValue: Int
)
