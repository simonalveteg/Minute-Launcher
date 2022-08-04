package com.example.android.minutelauncher

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import kotlinx.serialization.Serializable

@Serializable
data class UserApp(
  val packageName: String,
  val appTitle: String
)

fun ResolveInfo.toUserApp(packageManager: PackageManager) =
  UserApp(
    this.activityInfo.packageName,
    this.loadLabel(packageManager).toString()
  )
