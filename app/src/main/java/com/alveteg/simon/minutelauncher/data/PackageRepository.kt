package com.alveteg.simon.minutelauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class PackageRepository @Inject constructor(@ApplicationContext private val context: Context) {

  init {
    Timber.d("PackageRepository initialised")
  }

  private val packageManager = context.packageManager
  private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
  private var callback: LauncherApps.Callback? = null

  fun registerCallback(callback: LauncherApps.Callback) {
    launcherApps.registerCallback(callback)
  }

  fun unregisterCallback() {
    callback?.let {
      launcherApps.unregisterCallback(it)
      callback = null
    }
  }

  fun getPackages(): List<App> {
    return launcherApps.getActivityList(null, launcherApps.profiles.firstOrNull())
      .map { it.toApp() }
  }

  fun getLaunchIntentForPackage(packageName: String): Intent? {
    return packageManager.getLaunchIntentForPackage(packageName)?.apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    }
  }
}