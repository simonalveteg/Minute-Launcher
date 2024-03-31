package com.alveteg.simon.minutelauncher.data

import androidx.room.TypeConverter
import com.alveteg.simon.minutelauncher.utilities.Gesture

class Converters {
  @TypeConverter
  fun toGestureDirection(value: String) = Gesture.valueOf(value)

  @TypeConverter
  fun fromGestureDirection(value: Gesture) = value.name

  @TypeConverter
  fun toAccessTimer(value: String) = AccessTimer.valueOf(value)

  @TypeConverter
  fun fromAccessTimer(value: AccessTimer) = value.name
}