package com.example.android.minutelauncher.db

import androidx.room.TypeConverter
import com.example.android.minutelauncher.GestureDirection

class Converters {
  @TypeConverter
  fun toGestureDirection(value: String) = GestureDirection.valueOf(value)

  @TypeConverter
  fun fromGestureDirection(value: GestureDirection) = value.name
}