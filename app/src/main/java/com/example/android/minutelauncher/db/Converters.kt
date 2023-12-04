package com.example.android.minutelauncher.db

import androidx.room.TypeConverter
import com.example.android.minutelauncher.Gesture

class Converters {
  @TypeConverter
  fun toGestureDirection(value: String) = Gesture.valueOf(value)

  @TypeConverter
  fun fromGestureDirection(value: Gesture) = value.name
}