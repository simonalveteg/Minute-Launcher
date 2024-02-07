package com.example.android.minutelauncher.data

import androidx.room.TypeConverter
import com.example.android.minutelauncher.utilities.Gesture

class Converters {
  @TypeConverter
  fun toGestureDirection(value: String) = Gesture.valueOf(value)

  @TypeConverter
  fun fromGestureDirection(value: Gesture) = value.name
}